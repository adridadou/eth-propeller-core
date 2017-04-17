package org.adridadou.ethereum.propeller;

import org.adridadou.ethereum.propeller.event.EthereumEventHandler;
import org.adridadou.ethereum.propeller.event.OnTransactionParameters;
import org.adridadou.ethereum.propeller.event.TransactionReceipt;
import org.adridadou.ethereum.propeller.event.TransactionStatus;
import org.adridadou.ethereum.propeller.exception.EthereumApiException;
import org.adridadou.ethereum.propeller.solidity.SolidityContractDetails;
import org.adridadou.ethereum.propeller.solidity.SolidityEvent;
import org.adridadou.ethereum.propeller.solidity.SolidityType;
import org.adridadou.ethereum.propeller.solidity.abi.AbiParam;
import org.adridadou.ethereum.propeller.solidity.converters.SolidityTypeGroup;
import org.adridadou.ethereum.propeller.solidity.converters.decoders.SolidityTypeDecoder;
import org.adridadou.ethereum.propeller.solidity.converters.decoders.list.CollectionDecoder;
import org.adridadou.ethereum.propeller.solidity.converters.encoders.SolidityTypeEncoder;
import org.adridadou.ethereum.propeller.solidity.converters.encoders.list.CollectionEncoder;
import org.adridadou.ethereum.propeller.values.*;
import org.apache.commons.lang.ArrayUtils;
import rx.Observable;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.adridadou.ethereum.propeller.values.EthValue.wei;

/**
 * Created by davidroon on 20.04.16.
 * This code is released under Apache 2 license
 */
public class EthereumProxy {
    private static final int ADDITIONAL_GAS_FOR_CONTRACT_CREATION = 15_000;
    private static final int ADDITIONAL_GAS_DIRTY_FIX = 200_000;
    private static final long BLOCK_WAIT_LIMIT = 16;

    private final EthereumBackend ethereum;
    private final EthereumEventHandler eventHandler;
    private final Map<EthAddress, Set<EthHash>> pendingTransactions = new ConcurrentHashMap<>();
    private final Map<EthAddress, Nonce> nonces = new ConcurrentHashMap<>();
    private final Map<SolidityTypeGroup, List<SolidityTypeEncoder>> encoders = new HashMap<>();
    private final Map<SolidityTypeGroup, List<SolidityTypeDecoder>> decoders = new HashMap<>();
    private final List<Class<? extends CollectionDecoder>> listDecoders = new ArrayList<>();
    private final List<Class<? extends CollectionEncoder>> listEncoders = new ArrayList<>();

    EthereumProxy(EthereumBackend ethereum, EthereumEventHandler eventHandler) {
        this.ethereum = ethereum;
        this.eventHandler = eventHandler;
        updateNonce();
        ethereum.register(eventHandler);
    }

    EthereumProxy addEncoder(final SolidityTypeGroup typeGroup, final SolidityTypeEncoder encoder) {
        List<SolidityTypeEncoder> encoderList = encoders.computeIfAbsent(typeGroup, key -> new ArrayList<>());
        encoderList.add(encoder);
        return this;
    }

    EthereumProxy addListDecoder(final Class<? extends CollectionDecoder> decoder) {
        listDecoders.add(decoder);
        return this;
    }

    EthereumProxy addListEncoder(final Class<? extends CollectionEncoder> decoder) {
        listEncoders.add(decoder);
        return this;
    }

    EthereumProxy addDecoder(final SolidityTypeGroup typeGroup, final SolidityTypeDecoder decoder) {
        List<SolidityTypeDecoder> decoderList = decoders.computeIfAbsent(typeGroup, key -> new ArrayList<>());
        decoderList.add(decoder);
        return this;
    }

    CompletableFuture<EthAddress> publish(SolidityContractDetails contract, EthAccount account, Object... constructorArgs) {
        return createContract(contract, account, constructorArgs);
    }

    Nonce getNonce(final EthAddress address) {
        nonces.computeIfAbsent(address, ethereum::getNonce);
        Integer offset = Optional.ofNullable(pendingTransactions.get(address)).map(Set::size).orElse(0);
        return nonces.get(address).add(offset);
    }

    SmartContractByteCode getCode(EthAddress address) {
        return ethereum.getCode(address);
    }

    <T> Observable<T> observeEvents(SolidityEvent eventDefinition, EthAddress contractAddress, Class<T> cls) {
        return eventHandler.observeTransactions()
                .filter(params -> contractAddress.equals(params.receipt.receiveAddress))
                .flatMap(params -> Observable.from(params.getLogs()))
                .filter(eventDefinition::match)
                .map(data -> (T) eventDefinition.parseEvent(data, cls));
    }

    private CompletableFuture<EthAddress> publishContract(EthValue ethValue, EthData data, EthAccount account) {
        return this.sendTxInternal(ethValue, data, account, EthAddress.empty())
                .thenApply(receipt -> receipt.contractAddress);
    }

    CompletableFuture<EthExecutionResult> sendTx(EthValue value, EthData data, EthAccount account, EthAddress address) {
        return this.sendTxInternal(value, data, account, address)
                .thenApply(receipt -> new EthExecutionResult(receipt.executionResult));
    }

    public SmartContract getSmartContract(SolidityContractDetails details, EthAddress address, EthAccount account) {
        return new SmartContract(details, account, address, this, ethereum);
    }

    private CompletableFuture<EthAddress> createContract(SolidityContractDetails contract, EthAccount account, Object... constructorArgs) {
        EthData argsEncoded = new SmartContract(contract, account, EthAddress.empty(), this, ethereum).getConstructor(constructorArgs)
                .map(constructor -> constructor.encode(constructorArgs))
                .orElseGet(() -> {
                    if (constructorArgs.length > 0) {
                        throw new EthereumApiException("No constructor with params found");
                    }
                    return EthData.empty();
                });
        return publishContract(wei(0), EthData.of(ArrayUtils.addAll(contract.getBinary().data, argsEncoded.data)), account);

    }

    private CompletableFuture<TransactionReceipt> sendTxInternal(EthValue value, EthData data, EthAccount account, EthAddress toAddress) {
        return eventHandler.ready().thenCompose((v) -> {
            GasUsage gasLimit = estimateGas(value, data, account, toAddress);
            EthHash txHash = ethereum.submit(account, toAddress, value, data, getNonce(account.getAddress()), gasLimit);

            long currentBlock = eventHandler.getCurrentBlockNumber();

            CompletableFuture<TransactionReceipt> result = CompletableFuture.supplyAsync(() -> {
                Observable<OnTransactionParameters> droppedTxs = eventHandler.observeTransactions()
                        .filter(params -> params.receipt != null && Objects.equals(params.receipt.hash, txHash) && params.status == TransactionStatus.Dropped);
                Observable<OnTransactionParameters> timeoutBlock = eventHandler.observeBlocks()
                        .filter(blockParams -> blockParams.blockNumber > currentBlock + BLOCK_WAIT_LIMIT)
                        .map(params -> null);
                Observable<OnTransactionParameters> blockTxs = eventHandler.observeBlocks()
                        .flatMap(params -> Observable.from(params.receipts))
                        .filter(receipt -> Objects.equals(receipt.hash, txHash))
                        .map(this::createTransactionParameters);

                return Observable.merge(droppedTxs, blockTxs, timeoutBlock)
                        .map(params -> {
                            if (params == null) {
                                throw new EthereumApiException("the transaction has not been included in the last " + BLOCK_WAIT_LIMIT + " blocks");
                            }
                            TransactionReceipt receipt = params.receipt;
                            if (params.status == TransactionStatus.Dropped) {
                                throw new EthereumApiException("the transaction has been dropped! - " + params.receipt.error);
                            }
                            return checkForErrors(receipt);
                        }).toBlocking().first();

            });
            increasePendingTransactionCounter(account.getAddress(), txHash);
            return result;
        });
    }

    private GasUsage estimateGas(EthValue value, EthData data, EthAccount account, EthAddress toAddress) {
        GasUsage gasLimit = ethereum.estimateGas(account, toAddress, value, data);
        //if it is a contract creation
        if (toAddress.isEmpty()) {
            gasLimit = gasLimit.add(ADDITIONAL_GAS_FOR_CONTRACT_CREATION);
        }
        return gasLimit.add(ADDITIONAL_GAS_DIRTY_FIX);
    }

    private OnTransactionParameters createTransactionParameters(TransactionReceipt receipt) {
        return new OnTransactionParameters(receipt, TransactionStatus.Executed, new ArrayList<>());
    }

    private TransactionReceipt checkForErrors(final TransactionReceipt receipt) {
        if (receipt.isSuccessful) {
            return receipt;
        } else {
            throw new EthereumApiException("error with the transaction " + receipt.hash + ". error:" + receipt.error);
        }
    }

    private void updateNonce() {
        eventHandler.observeTransactions()
                .filter(tx -> tx.status == TransactionStatus.Dropped)
                .forEach(params -> {
                    EthAddress currentAddress = params.receipt.sender;
                    EthHash hash = params.receipt.hash;
                    Optional.ofNullable(pendingTransactions.get(currentAddress)).ifPresent(hashes -> {
                        hashes.remove(hash);
                        nonces.put(currentAddress, ethereum.getNonce(currentAddress));
                    });
                });
        eventHandler.observeBlocks()
                .forEach(params -> params.receipts
                        .forEach(receipt -> Optional.ofNullable(pendingTransactions.get(receipt.sender))
                                .ifPresent(hashes -> {
                                    hashes.remove(receipt.hash);
                                    nonces.put(receipt.sender, ethereum.getNonce(receipt.sender));
                                })));
    }

    EthereumEventHandler events() {
        return eventHandler;
    }

    boolean addressExists(final EthAddress address) {
        return ethereum.addressExists(address);
    }

    EthValue getBalance(final EthAddress address) {
        return ethereum.getBalance(address);
    }

    private void increasePendingTransactionCounter(EthAddress address, EthHash hash) {
        Set<EthHash> hashes = pendingTransactions.computeIfAbsent(address, (key) -> Collections.synchronizedSet(new HashSet<>()));
        hashes.add(hash);
        pendingTransactions.put(address, hashes);
    }

    public List<SolidityTypeEncoder> getEncoders(AbiParam abiParam) {
        SolidityType type = SolidityType.find(abiParam.getType())
                .orElseThrow(() -> new EthereumApiException("unknown type " + abiParam.getType()));
        if (abiParam.isArray()) {
            return listEncoders.stream().map(cls -> {
                try {
                    if (abiParam.isDynamic()) {
                        return cls.getConstructor(List.class).newInstance(getEncoders(type, abiParam));
                    }
                    return cls.getConstructor(List.class, Integer.class).newInstance(getEncoders(type, abiParam), abiParam.getArraySize());
                } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                    throw new EthereumApiException("error while preparing list encoders", e);
                }
            }).collect(Collectors.toList());
        }
        return getEncoders(type, abiParam);
    }

    private List<SolidityTypeEncoder> getEncoders(final SolidityType type, AbiParam abiParam) {
        return Optional.ofNullable(encoders.get(SolidityTypeGroup.resolveGroup(type))).orElseThrow(() -> new EthereumApiException("no encoder found for solidity type " + abiParam.getType()));
    }

    List<SolidityTypeDecoder> getDecoders(AbiParam abiParam) {
        SolidityType type = SolidityType.find(abiParam.getType())
                .orElseThrow(() -> new EthereumApiException("unknown type " + abiParam.getType()));

        SolidityTypeGroup typeGroup = SolidityTypeGroup.resolveGroup(type);

        if (abiParam.isArray()) {
            return listDecoders.stream().map(cls -> {
                try {
                    return cls.getConstructor(List.class, Integer.class).newInstance(decoders.get(typeGroup), abiParam.getArraySize());
                } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                    throw new EthereumApiException("error while creating a List decoder");
                }
            }).collect(Collectors.toList());
        }

        return Optional.ofNullable(decoders.get(typeGroup))
                .orElseThrow(() -> new EthereumApiException("no decoder found for solidity type " + abiParam.getType()));
    }
}

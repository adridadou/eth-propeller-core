package org.adridadou.ethereum.propeller;

import org.adridadou.ethereum.propeller.event.BlockInfo;
import org.adridadou.ethereum.propeller.event.EthereumEventHandler;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static org.adridadou.ethereum.propeller.values.EthValue.wei;

/**
 * Created by davidroon on 20.04.16.
 * This code is released under Apache 2 license
 */
class EthereumProxy {
    private static final int ADDITIONAL_GAS_FOR_CONTRACT_CREATION = 15_000;
    private static final int ADDITIONAL_GAS_DIRTY_FIX = 200_000;
    private static final Logger logger = LoggerFactory.getLogger(EthereumProxy.class);

    private final BlockingQueue<TransactionRequest> transactions = new ArrayBlockingQueue<>(10000);
    private final Map<TransactionRequest, CompletableFuture<EthHash>> futureMap = new LinkedHashMap<>();

    private final EthereumBackend ethereum;
    private final EthereumEventHandler eventHandler;
    private final EthereumConfig config;
    private final Map<EthAddress, Set<EthHash>> pendingTransactions = new HashMap<>();
    private final Map<EthAddress, Nonce> nonces = new HashMap<>();
    private final Map<SolidityTypeGroup, List<SolidityTypeEncoder>> encoders = new HashMap<>();
    private final Map<SolidityTypeGroup, List<SolidityTypeDecoder>> decoders = new HashMap<>();
    private final List<Class<? extends CollectionDecoder>> listDecoders = new ArrayList<>();
    private final List<Class<? extends CollectionEncoder>> listEncoders = new ArrayList<>();
    private final Set<Class<?>> voidClasses = new HashSet<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final ReentrantLock nonceLock = new ReentrantLock();
    private final ReentrantLock txLock = new ReentrantLock();

    EthereumProxy(EthereumBackend ethereum, EthereumEventHandler eventHandler, EthereumConfig config) {
        this.ethereum = ethereum;
        this.eventHandler = eventHandler;
        this.config = config;
        updateNonce();
        ethereum.register(eventHandler);
        processTransactions();
    }

    private void processTransactions() {
        executor.submit(() -> {
            while (true) {
                try {
                    TransactionRequest request = transactions.take();
                    this.processTransactionRequest(request);
                } catch (Throwable e) {
                    logger.warn("Interrupted error while waiting for transactions to be submitted:", e);
                }
            }
        });
    }

    private void processTransactionRequest(TransactionRequest request) {
        Nonce nonce = getNonce(request.getAccount().getAddress());
        EthHash hash = ethereum.submit(request, nonce);
        increasePendingTransactionCounter(request.getAccount().getAddress(), hash);
        txLock.lock();
        futureMap.get(request).complete(hash);
        futureMap.remove(request);
        txLock.unlock();
    }

    EthereumProxy addVoidClass(Class<?> cls) {
        voidClasses.add(cls);
        return this;
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

    CompletableFuture<EthAddress> publishWithValue(SolidityContractDetails contract, EthAccount account, EthValue value, Object... constructorArgs) {
        return createContractWithValue(contract, account, value, constructorArgs);
    }

    CompletableFuture<EthAddress> publish(SolidityContractDetails contract, EthAccount account, Object... constructorArgs) {
        return createContract(contract, account, constructorArgs);
    }

    Nonce getNonce(final EthAddress address) {
        nonceLock.lock();
        nonces.computeIfAbsent(address, ethereum::getNonce);
        Integer offset = Optional.ofNullable(pendingTransactions.get(address)).map(Set::size).orElse(0);
        Nonce nonce = nonces.get(address).add(offset);
        nonceLock.unlock();
        return nonce;
    }

    SmartContractByteCode getCode(EthAddress address) {
        return ethereum.getCode(address);
    }

    <T> Observable<T> observeEvents(SolidityEvent<T> eventDefinition, EthAddress contractAddress) {
        return observeEventsWithInfo(eventDefinition, contractAddress).map(EventInfo::getResult);
    }

    <T> Observable<EventInfo<T>> observeEventsWithInfo(SolidityEvent<T> eventDefinition, EthAddress contractAddress) {
        return eventHandler.observeTransactions()
                .filter(params -> params.getReceipt().map(receipt -> contractAddress.equals(receipt.receiveAddress)).orElse(false))
                .flatMap(params -> Observable.from(params.getReceipt().map(receipt -> receipt.events).get().stream().filter(eventDefinition::match)
                        .map(data -> new EventInfo<>(params.getTransactionHash(), eventDefinition.parseEvent(data, eventDefinition.getEntityClass()))).collect(Collectors.toList())));
    }

    private CompletableFuture<EthAddress> publishContract(EthValue ethValue, EthData data, EthAccount account) {
        return this.sendTxInternal(ethValue, data, account, EthAddress.empty())
                .thenCompose(CallDetails::getResult)
                .thenApply(receipt -> receipt.contractAddress);
    }

    CompletableFuture<CallDetails> sendTx(EthValue value, EthData data, EthAccount account, EthAddress address) {
        return this.sendTxInternal(value, data, account, address);
    }

    public SmartContract getSmartContract(SolidityContractDetails details, EthAddress address, EthAccount account) {
        return new SmartContract(details, account, address, this, ethereum);
    }

    private CompletableFuture<EthAddress> createContract(SolidityContractDetails contract, EthAccount account, Object... constructorArgs) {
        return createContractWithValue(contract, account, wei(0), constructorArgs);
    }

    private CompletableFuture<EthAddress> createContractWithValue(SolidityContractDetails contract, EthAccount account, EthValue value, Object... constructorArgs) {
        EthData argsEncoded = new SmartContract(contract, account, EthAddress.empty(), this, ethereum).getConstructor(constructorArgs)
                .map(constructor -> constructor.encode(constructorArgs))
                .orElseGet(() -> {
                    if (constructorArgs.length > 0) {
                        throw new EthereumApiException("No constructor found with params (" + printTypes(constructorArgs) + ")");
                    }
                    return EthData.empty();
                });
        return publishContract(value, EthData.of(ArrayUtils.addAll(contract.getBinary().data, argsEncoded.data)), account);

    }

    private String printTypes(Object[] constructorArgs) {
        return Arrays.stream(constructorArgs).map(arg -> {
            if (arg == null) {
                return "null";
            } else {
                return arg.getClass().getSimpleName();
            }
        }).reduce((a, b) -> a + ", " + b).orElse("[no args]");
    }

    private CompletableFuture<EthHash> submitTransaction(TransactionRequest request) {
        CompletableFuture<EthHash> future = new CompletableFuture<>();
        txLock.lock();
        transactions.add(request);
        futureMap.put(request, future);
        txLock.unlock();
        return future;
    }

    private CompletableFuture<CallDetails> sendTxInternal(EthValue value, EthData data, EthAccount account, EthAddress toAddress) {
        return eventHandler.ready().thenCompose((v) -> {
            GasUsage gasLimit = estimateGas(value, data, account, toAddress);
            GasPrice gasPrice = ethereum.getGasPrice();

            return submitTransaction(new TransactionRequest(account, toAddress, value, data, gasLimit, gasPrice))
                    .thenApply(txHash -> new CallDetails(this.waitForResult(txHash), txHash));
        });
    }

    private CompletableFuture<TransactionReceipt> waitForResult(EthHash txHash) {
        Objects.requireNonNull(txHash);
        long currentBlock = eventHandler.getCurrentBlockNumber();

        Observable<TransactionInfo> droppedTxs = eventHandler.observeTransactions()
                .filter(params -> params.getReceipt().map(receipt -> Objects.equals(receipt.hash, txHash)).orElse(false) && params.getStatus() == TransactionStatus.Dropped);
        Observable<TransactionInfo> timeoutBlock = eventHandler.observeBlocks()
                .filter(blockParams -> blockParams.blockNumber > currentBlock + config.blockWaitLimit())
                .map(params -> null);

        Observable<TransactionInfo> blockTxs = eventHandler.observeBlocks()
                .flatMap(params -> Observable.from(params.receipts))
                .filter(receipt -> Objects.equals(receipt.hash, txHash))
                .map(this::createTransactionParameters);

        Observable<TransactionInfo> observeTx = Observable.interval(10, TimeUnit.SECONDS)
                .map(x -> getTransactionInfo(txHash))
                .filter(tx -> tx
                        .map(TransactionInfo::getStatus)
                        .map(TransactionStatus.Executed::equals).orElse(false))
                .filter(Optional::isPresent)
                .map(Optional::get);

        CompletableFuture<TransactionReceipt> futureResult = new CompletableFuture<>();

        Observable.merge(droppedTxs, blockTxs, timeoutBlock, observeTx)
                .map(params -> {
                    if (params == null) {
                        throw new EthereumApiException("the transaction has not been included in the last " + config.blockWaitLimit() + " blocks");
                    }
                    TransactionReceipt receipt = params.getReceipt().orElseThrow(() -> new EthereumApiException("no Transaction receipt found!"));
                    if (params.getStatus() == TransactionStatus.Dropped) {
                        throw new EthereumApiException("the transaction has been dropped! - " + receipt.error);
                    }
                    Optional<TransactionReceipt> result = checkForErrors(receipt);
                    return result.orElseThrow(() -> new EthereumApiException("error with the transaction " + receipt.hash + ". error:" + receipt.error));
                }).first().forEach(futureResult::complete);

        return futureResult;
    }

    private GasUsage estimateGas(EthValue value, EthData data, EthAccount account, EthAddress toAddress) {
        GasUsage gasLimit = ethereum.estimateGas(account, toAddress, value, data);
        //if it is a contract creation
        if (toAddress.isEmpty()) {
            gasLimit = gasLimit.add(ADDITIONAL_GAS_FOR_CONTRACT_CREATION);
        }
        return gasLimit.add(ADDITIONAL_GAS_DIRTY_FIX);
    }

    private TransactionInfo createTransactionParameters(TransactionReceipt receipt) {
        return new TransactionInfo(receipt.hash, receipt, TransactionStatus.Executed);
    }

    private Optional<TransactionReceipt> checkForErrors(final TransactionReceipt receipt) {
        if (receipt.isSuccessful) {
            return Optional.of(receipt);
        } else {
            return Optional.empty();
        }
    }

    private void updateNonce() {
        eventHandler.observeTransactions()
                .filter(tx -> tx.getStatus() == TransactionStatus.Dropped)
                .forEach(params -> {
                    TransactionReceipt receipt = params.getReceipt().orElseThrow(() -> new EthereumApiException("no Transaction receipt found!"));
                    EthAddress currentAddress = receipt.sender;
                    EthHash hash = receipt.hash;
                    nonceLock.lock();
                    Optional.ofNullable(pendingTransactions.get(currentAddress)).ifPresent(hashes -> {
                        hashes.remove(hash);
                        nonces.put(currentAddress, ethereum.getNonce(currentAddress));
                    });
                    nonceLock.unlock();
                });
        eventHandler.observeBlocks()
                .forEach(params -> {
                    nonceLock.lock();
                    params.receipts
                            .forEach(receipt -> Optional.ofNullable(pendingTransactions.get(receipt.sender))
                                    .ifPresent(hashes -> {
                                        hashes.remove(receipt.hash);
                                        nonces.put(receipt.sender, ethereum.getNonce(receipt.sender));
                                    }));
                    nonceLock.unlock();
                });
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

    List<SolidityTypeEncoder> getEncoders(AbiParam abiParam) {
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

        if (abiParam.isArray() || type.equals(SolidityType.BYTES)) {
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

    public <T> boolean isVoidType(Class<T> cls) {
        return voidClasses.contains(cls);
    }

    public <T> List<T> getEventsAtBlock(SolidityEvent eventDefinition, EthAddress address, Class<T> cls, Long blockNumber) {
        return getEventsAtBlock(eventDefinition, address, cls, ethereum.getBlock(blockNumber));
    }

    public <T> List<T> getEventsAtBlock(SolidityEvent eventDefinition, EthAddress address, Class<T> cls, EthHash blockHash) {
        return getEventsAtBlock(eventDefinition, address, cls, ethereum.getBlock(blockHash));
    }

    private <T> List<T> getEventsAtBlock(SolidityEvent eventDefinition, EthAddress address, Class<T> cls, BlockInfo blockInfo) {
        return getEventsAtBlockWithInfo(eventDefinition, address, cls, blockInfo).stream()
                .map(EventInfo::getResult)
                .collect(Collectors.toList());
    }

    public <T> List<EventInfo<T>> getEventsAtBlockWithInfo(SolidityEvent eventDefinition, EthAddress address, Class<T> cls, Long blockNumber) {
        return getEventsAtBlockWithInfo(eventDefinition, address, cls, ethereum.getBlock(blockNumber));
    }

    public <T> List<EventInfo<T>> getEventsAtBlockWithInfo(SolidityEvent eventDefinition, EthAddress address, Class<T> cls, EthHash blockHash) {
        return getEventsAtBlockWithInfo(eventDefinition, address, cls, ethereum.getBlock(blockHash));
    }

    private <T> List<EventInfo<T>> getEventsAtBlockWithInfo(SolidityEvent eventDefinition, EthAddress address, Class<T> cls, BlockInfo blockInfo) {
        return blockInfo.receipts.stream()
                .filter(params -> address.equals(params.receiveAddress))
                .flatMap(params -> params.events.stream())
                .filter(eventDefinition::match)
                .map(data -> new EventInfo<>(data.getTransactionHash(), (T) eventDefinition.parseEvent(data, cls))).collect(Collectors.toList());
    }


    public <T> List<T> getEventsAtTransaction(SolidityEvent eventDefinition, EthAddress address, Class<T> cls, EthHash transactionHash) {
        return getEventsAtTransactionWithInfo(eventDefinition, address, cls, transactionHash).stream()
                .map(EventInfo::getResult).collect(Collectors.toList());
    }

    public <T> List<EventInfo<T>> getEventsAtTransactionWithInfo(SolidityEvent eventDefinition, EthAddress address, Class<T> cls, EthHash transactionHash) {
        TransactionReceipt receipt = ethereum.getTransactionInfo(transactionHash).flatMap(TransactionInfo::getReceipt).orElseThrow(() -> new EthereumApiException("no Transaction receipt found!"));
        if (address.equals(receipt.receiveAddress)) {
            return receipt.events.stream().filter(eventDefinition::match)
                    .map(data -> new EventInfo<>(data.getTransactionHash(), (T) eventDefinition.parseEvent(data, cls)))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }

    public long getCurrentBlockNumber() {
        return eventHandler.getCurrentBlockNumber();
    }

    public Optional<TransactionInfo> getTransactionInfo(EthHash hash) {
        return ethereum.getTransactionInfo(hash);
    }
}

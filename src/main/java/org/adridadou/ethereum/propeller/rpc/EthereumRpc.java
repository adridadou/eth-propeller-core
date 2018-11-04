package org.adridadou.ethereum.propeller.rpc;

import org.adridadou.ethereum.propeller.EthereumBackend;
import org.adridadou.ethereum.propeller.event.BlockInfo;
import org.adridadou.ethereum.propeller.event.EthereumEventHandler;
import org.adridadou.ethereum.propeller.values.*;
import org.ethereum.crypto.ECKey;
import org.ethereum.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.Transaction;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by davidroon on 20.01.17.
 * This code is released under Apache 2 license
 */
public class EthereumRpc implements EthereumBackend {
    private static final Logger logger = LoggerFactory.getLogger(EthereumRpc.class);

    private final Web3JFacade web3JFacade;
    private final EthereumRpcEventGenerator ethereumRpcEventGenerator;
    private final ChainId chainId;

    public EthereumRpc(Web3JFacade web3JFacade, ChainId chainId, EthereumRpcConfig config) {
        this.web3JFacade = web3JFacade;
        this.ethereumRpcEventGenerator = new EthereumRpcEventGenerator(web3JFacade, config, this);
        this.chainId = chainId;
    }

    @Override
    public GasPrice getGasPrice() {
        return web3JFacade.getGasPrice();
    }

    @Override
    public EthValue getBalance(EthAddress address) {
        return EthValue.wei(web3JFacade.getBalance(address).getBalance());
    }

    @Override
    public boolean addressExists(EthAddress address) {
        return web3JFacade.getTransactionCount(address).intValue() > 0 || web3JFacade.getBalance(address).getBalance().intValue() > 0 || !web3JFacade.getCode(address).isEmpty();
    }

    @Override
    public EthHash submit(TransactionRequest request, Nonce nonce) {
        org.ethereum.core.Transaction transaction = createTransaction(nonce, getGasPrice(), request);
        transaction.sign(ECKey.fromPrivate(request.getAccount().getBigIntPrivateKey()));
        web3JFacade.sendTransaction(EthData.of(transaction.getEncoded()));
        return EthHash.of(transaction.getHash());
    }

    private org.ethereum.core.Transaction createTransaction(Nonce nonce, GasPrice gasPrice, TransactionRequest request) {
        byte[] nonceBytes = encodeBigInt(nonce.getValue());
        byte[] gasPriceBytes = encodeBigInt(gasPrice.getPrice().inWei());
        byte[] gasBytes = encodeBigInt(request.getGasLimit().getUsage());
        byte[] valueBytes = encodeBigInt(request.getValue().inWei());

        return new org.ethereum.core.Transaction(nonceBytes, gasPriceBytes, gasBytes,
                request.getAddress().address, valueBytes, request.getData().data, chainId.id);
    }

    private byte[] encodeBigInt(BigInteger value) {
        if(BigInteger.ZERO.equals(value)){
            return ByteUtil.EMPTY_BYTE_ARRAY;
        }
        return ByteUtil.bigIntegerToBytes(value);
    }

    @Override
    public GasUsage estimateGas(EthAccount account, EthAddress address, EthValue value, EthData data) {
        return new GasUsage(web3JFacade.estimateGas(account, address, value, data));
    }

    @Override
    public Nonce getNonce(EthAddress currentAddress) {
        return new Nonce(web3JFacade.getTransactionCount(currentAddress));
    }

    @Override
    public long getCurrentBlockNumber() {
        return web3JFacade.getCurrentBlockNumber();
    }

    @Override
    public Optional<BlockInfo> getBlock(long number) {
        return web3JFacade.getBlock(number).map(this::toBlockInfo);
    }

    @Override
    public Optional<BlockInfo> getBlock(EthHash ethHash) {
        return web3JFacade.getBlock(ethHash).map(this::toBlockInfo);
    }

    @Override
    public SmartContractByteCode getCode(EthAddress address) {
        return web3JFacade.getCode(address);
    }

    @Override
    public EthData constantCall(EthAccount account, EthAddress address, EthValue value, EthData data) {
        return web3JFacade.constantCall(account, address, data);
    }

    @Override
    public void register(EthereumEventHandler eventHandler) {
        ethereumRpcEventGenerator.addListener(eventHandler);
    }

    @Override
    public Optional<TransactionInfo> getTransactionInfo(EthHash hash) {
        System.out.println("getting info for " + hash.withLeading0x());
        return Optional.ofNullable(web3JFacade.getReceipt(hash)).flatMap(web3jReceipt -> Optional.ofNullable(web3JFacade.getTransaction(hash))
                .map(transaction -> {
                    TransactionReceipt receipt = toReceipt(transaction, web3jReceipt);
                    TransactionStatus status = transaction.getBlockHash().isEmpty() ? TransactionStatus.Unknown : TransactionStatus.Executed;
                    System.out.println("getting receipt " + receipt.isSuccessful + ":" + receipt.error + ":" + status.name());
                    return new TransactionInfo(hash, receipt, status, EthHash.of(transaction.getBlockHash()));
                })
        );
    }

    BlockInfo toBlockInfo(EthBlock ethBlock) {
        return Optional.ofNullable(ethBlock.getBlock()).map(block -> {
            try {
                Map<String, EthBlock.TransactionObject> txObjects = block.getTransactions().stream()
                        .map(tx -> (EthBlock.TransactionObject) tx.get()).collect(Collectors.toMap(EthBlock.TransactionObject::getHash, e -> e));

                Map<String, org.web3j.protocol.core.methods.response.TransactionReceipt> receipts = txObjects.values().stream()
                        .map(tx -> Optional.ofNullable(web3JFacade.getReceipt(EthHash.of(tx.getHash()))))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toMap(org.web3j.protocol.core.methods.response.TransactionReceipt::getTransactionHash, e -> e));

                List<TransactionReceipt> receiptList = receipts.entrySet().stream()
                        .map(entry -> toReceipt(txObjects.get(entry.getKey()), entry.getValue())).collect(Collectors.toList());

                return new BlockInfo(block.getNumber().longValue(), receiptList);
            } catch (Throwable ex) {
                logger.error("error while converting to block info", ex);
                return new BlockInfo(block.getNumber().longValue(), Collections.emptyList());
            }
        }).orElseGet(() -> new BlockInfo(-1, new ArrayList<>()));
    }

    private TransactionReceipt toReceipt(Transaction tx, org.web3j.protocol.core.methods.response.TransactionReceipt receipt) {
        boolean successful = !receipt.getGasUsed().equals(tx.getGas());
        String error = "";
        if (!successful) {
            error = "All the gas was used! an error occurred";
        }

        return new TransactionReceipt(EthHash.of(receipt.getTransactionHash()), EthHash.of(receipt.getBlockHash()), EthAddress.of(receipt.getFrom()), EthAddress.of(receipt.getTo()), EthAddress.of(receipt.getContractAddress()), EthData.of(tx.getInput()), error, EthData.empty(), successful, toEventInfos(EthHash.of(receipt.getTransactionHash()), receipt.getLogs()), EthValue.wei(tx.getValue()));
    }

    private List<EventData> toEventInfos(EthHash transactionHash, List<Log> logs) {
        return logs.stream().map(log -> this.toEventInfo(transactionHash, log)).collect(Collectors.toList());
    }

    private EventData toEventInfo(EthHash transactionHash, Log log) {
        List<EthData> topics = log.getTopics().stream().map(EthData::of).collect(Collectors.toList());
        if(topics.size() > 0) {
            EthData eventSignature = topics.get(0);
            EthData eventArguments = EthData.of(log.getData());
            return new EventData(transactionHash, eventSignature, eventArguments, topics.subList(1, topics.size()));
        } else {
            return new EventData(transactionHash, EthData.empty(), EthData.empty(), new ArrayList<>());
        }
    }
}

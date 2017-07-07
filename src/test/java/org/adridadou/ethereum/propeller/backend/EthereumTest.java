package org.adridadou.ethereum.propeller.backend;

import org.adridadou.ethereum.propeller.EthereumBackend;
import org.adridadou.ethereum.propeller.event.BlockInfo;
import org.adridadou.ethereum.propeller.event.EthereumEventHandler;
import org.adridadou.ethereum.propeller.exception.EthereumApiException;
import org.adridadou.ethereum.propeller.values.*;
import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.crypto.ECKey;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.blockchain.StandaloneBlockchain;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Created by davidroon on 20.01.17.
 * This code is released under Apache 2 license
 */
public class EthereumTest implements EthereumBackend {
    private final StandaloneBlockchain blockchain;
    private final TestConfig testConfig;
    private final BlockingQueue<Transaction> transactions = new ArrayBlockingQueue<>(100);
    private final LocalExecutionService localExecutionService;

    public EthereumTest(TestConfig testConfig) {
        this.blockchain = new StandaloneBlockchain();

        blockchain
                .withGasLimit(testConfig.getGasLimit())
                .withGasPrice(testConfig.getGasPrice())
                .withCurrentTime(testConfig.getInitialTime());

        testConfig.getBalances().forEach((key, value) -> blockchain.withAccountBalance(key.getAddress().address, value.inWei()));

        localExecutionService = new LocalExecutionService(blockchain.getBlockchain());
        CompletableFuture.runAsync(() -> {
            try {
                while (true) {
                    blockchain.submitTransaction(transactions.take());
                    blockchain.createBlock();
                }
            } catch (InterruptedException e) {
                throw new EthereumApiException("error while polling transactions for test env", e);
            }
        });

        this.testConfig = testConfig;
    }

    @Override
    public GasPrice getGasPrice() {
        return new GasPrice(BigInteger.valueOf(testConfig.getGasPrice()));
    }

    @Override
    public EthValue getBalance(EthAddress address) {
        return EthValue.wei(blockchain.getBlockchain().getRepository().getBalance(address.address));
    }

    @Override
    public boolean addressExists(EthAddress address) {
        return blockchain.getBlockchain().getRepository().isExist(address.address);
    }

    @Override
    public EthHash submit(TransactionRequest request, Nonce nonce) {
        Transaction tx = createTransaction(request, nonce);
        this.transactions.add(tx);
        return EthHash.of(tx.getHash());
    }

    @Override
    public EthHash getTransactionHash(TransactionRequest request, Nonce nonce) {
        return EthHash.of(createTransaction(request, nonce).getHash());
    }

    private Transaction createTransaction(TransactionRequest request, Nonce nonce) {
        Transaction transaction = new Transaction(ByteUtil.bigIntegerToBytes(nonce.getValue()), ByteUtil.bigIntegerToBytes(BigInteger.ZERO), ByteUtil.bigIntegerToBytes(request.getGasLimit().getUsage()), request.getAddress().address, ByteUtil.bigIntegerToBytes(request.getValue().inWei()), request.getData().data, null);
        transaction.sign(getKey(request.getAccount()));
        return transaction;
    }

    @Override
    public GasUsage estimateGas(final EthAccount account, final EthAddress address, final EthValue value, final EthData data) {
        return localExecutionService.estimateGas(account, address, value, data);
    }

    @Override
    public Nonce getNonce(EthAddress currentAddress) {
        return new Nonce(blockchain.getBlockchain().getRepository().getNonce(currentAddress.address));
    }

    @Override
    public long getCurrentBlockNumber() {
        return blockchain.getBlockchain().getBestBlock().getNumber();
    }

    @Override
    public BlockInfo getBlock(long blockNumber) {
        return toBlockInfo(blockchain.getBlockchain().getBlockByNumber(blockNumber));
    }

    @Override
    public BlockInfo getBlock(EthHash blockNumber) {
        return toBlockInfo(blockchain.getBlockchain().getBlockByHash(blockNumber.data));
    }

    @Override
    public SmartContractByteCode getCode(EthAddress address) {
        return SmartContractByteCode.of(blockchain.getBlockchain().getRepository().getCode(address.address));
    }

    @Override
    public synchronized EthData constantCall(final EthAccount account, final EthAddress address, final EthValue value, final EthData data) {
        return localExecutionService.executeLocally(account, address, value, data);
    }

    @Override
    public void register(EthereumEventHandler eventHandler) {
        eventHandler.onReady();
        blockchain.addEthereumListener(new EthJEventListener(eventHandler));
    }

    @Override
    public Optional<TransactionInfo> getTransactionInfo(EthHash hash) {
        return Optional.ofNullable(blockchain.getBlockchain().getTransactionInfo(hash.data)).map(info -> {
            EthHash blockHash = EthHash.of(info.getBlockHash());
            TransactionStatus status = info.isPending() ? TransactionStatus.Pending : blockHash.isEmpty() ? TransactionStatus.Unknown : TransactionStatus.Executed;
            return new TransactionInfo(hash, EthJEventListener.toReceipt(info.getReceipt(), blockHash), status);
        });
    }

    private ECKey getKey(EthAccount account) {
        return ECKey.fromPrivate(account.getBigIntPrivateKey());
    }

    BlockInfo toBlockInfo(Block block) {
        return new BlockInfo(block.getNumber(), block.getTransactionsList().stream().map(tx -> this.toReceipt(tx, EthHash.of(block.getHash()))).collect(Collectors.toList()));
    }

    private TransactionReceipt toReceipt(Transaction tx, EthHash blockHash) {
        return new TransactionReceipt(EthHash.of(tx.getHash()), blockHash, EthAddress.of(tx.getSender()), EthAddress.of(tx.getReceiveAddress()), EthAddress.empty(), "", EthData.empty(), true, Collections.emptyList());
    }
}

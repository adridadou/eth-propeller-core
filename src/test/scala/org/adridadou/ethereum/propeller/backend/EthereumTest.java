package org.adridadou.ethereum.propeller.backend;

import org.adridadou.ethereum.propeller.EthereumBackend;
import org.adridadou.ethereum.propeller.event.EthereumEventHandler;
import org.adridadou.ethereum.propeller.exception.EthereumApiException;
import org.adridadou.ethereum.propeller.keystore.AccountProvider;
import org.adridadou.ethereum.propeller.values.*;
import org.ethereum.core.Transaction;
import org.ethereum.crypto.ECKey;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.blockchain.StandaloneBlockchain;

import java.math.BigInteger;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;

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

    public EthAccount defaultAccount() {
        return AccountProvider.fromPrivateKey(this.blockchain.getSender().getPrivKey());
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
    public EthHash submit(EthAccount account, EthAddress address, EthValue value, EthData data, Nonce nonce, GasUsage gasLimit) {
        Transaction tx = createTransaction(account, nonce, gasLimit, address, value, data);
        this.transactions.add(tx);
        return EthHash.of(tx.getHash());
    }

    private Transaction createTransaction(EthAccount account, Nonce nonce, GasUsage gasLimit, EthAddress address, EthValue value, EthData data) {
        Transaction transaction = new Transaction(ByteUtil.bigIntegerToBytes(nonce.getValue()), ByteUtil.bigIntegerToBytes(BigInteger.ZERO), ByteUtil.bigIntegerToBytes(gasLimit.getUsage()), address.address, ByteUtil.bigIntegerToBytes(value.inWei()), data.data, null);
        transaction.sign(getKey(account));
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
    public SmartContractByteCode getCode(EthAddress address) {
        return SmartContractByteCode.of(blockchain.getBlockchain().getRepository().getCode(address.address));
    }

    @Override
    public EthData constantCall(final EthAccount account, final EthAddress address, final EthValue value, final EthData data) {
        return localExecutionService.executeLocally(account, address, value, data);
    }

    @Override
    public void register(EthereumEventHandler eventHandler) {
        eventHandler.onReady();
        blockchain.addEthereumListener(new EthJEventListener(eventHandler));
    }

    private ECKey getKey(EthAccount account) {
        return ECKey.fromPrivate(account.getBigIntPrivateKey());
    }
}

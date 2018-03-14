package org.adridadou.ethereum.propeller.rpc;

import org.adridadou.ethereum.propeller.exception.EthereumApiException;
import org.adridadou.ethereum.propeller.values.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.RawTransaction;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Numeric;
import rx.Observable;

import java.io.IOError;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Optional;
import java.util.concurrent.Executors;

/**
 * Created by davidroon on 19.11.16.
 * This code is released under Apache 2 license
 */
public class Web3JFacade {
    private static final BigInteger GAS_LIMIT_FOR_CONSTANT_CALLS = BigInteger.valueOf(1_000_000_000);
    private static final Logger logger = LoggerFactory.getLogger(Web3JFacade.class);
    private final Web3j web3j;
    private final Web3jBlockHandler blockEventHandler = new Web3jBlockHandler();
    private BigInteger lastBlockNumber = BigInteger.ZERO;

    public Web3JFacade(final Web3j web3j) {
        this.web3j = web3j;
    }

    EthData constantCall(final EthAccount account, final EthAddress address, final EthData data) {
        try {
            return EthData.of(handleError(web3j.ethCall(new Transaction(
                    account.getAddress().withLeading0x(),
                    BigInteger.ZERO,
                    BigInteger.ZERO,
                    GAS_LIMIT_FOR_CONSTANT_CALLS,
                    address.withLeading0x(),
                    BigInteger.ZERO,
                    data.toString()
            ), DefaultBlockParameterName.LATEST).send()));
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    BigInteger getTransactionCount(EthAddress address) {
        try {
            return Numeric.decodeQuantity(handleError(web3j.ethGetTransactionCount(address.withLeading0x(), DefaultBlockParameterName.LATEST).send()));
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    Observable<EthBlock> observeBlocks() {
        return web3j.blockObservable(true);
    }

    Observable<EthBlock> observeBlocksPolling(long pollingFrequence) {
        Executors.newCachedThreadPool().submit(() -> {
            while (true) {
                try {
                    BigInteger currentBlockNumber = web3j.ethBlockNumber().send().getBlockNumber();
                    while (this.lastBlockNumber.equals(BigInteger.ZERO) || currentBlockNumber.compareTo(this.lastBlockNumber) < 0) {
                        EthBlock currentBlock = web3j.ethGetBlockByNumber(DefaultBlockParameter.valueOf(this.lastBlockNumber.add(BigInteger.ONE)), true).send();
                        this.lastBlockNumber = currentBlockNumber;
                        blockEventHandler.newElement(currentBlock);
                    }
                } catch (Throwable e) {
                    logger.warn("error while polling blocks", e);
                }
                Thread.sleep(pollingFrequence);
            }
        });
        return blockEventHandler.observable;
    }

    BigInteger estimateGas(EthAccount account, EthAddress address, EthValue value, EthData data) {
        try {
            return Numeric.decodeQuantity(handleError(web3j.ethEstimateGas(new Transaction(account.getAddress().withLeading0x(), null, null, null,
                    address.isEmpty() ? null : address.withLeading0x(), value.inWei(), data.toString())).send()));
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    GasPrice getGasPrice() {
        try {
            return new GasPrice(EthValue.wei(Numeric.decodeQuantity(handleError(web3j.ethGasPrice().send()))));
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    EthHash sendTransaction(final EthData rawTransaction) {
        try {
            return EthHash.of(handleError(web3j.ethSendRawTransaction(rawTransaction.withLeading0x()).send()));
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    public EthGetBalance getBalance(EthAddress address) {
        try {
            return web3j.ethGetBalance(address.withLeading0x(), DefaultBlockParameterName.LATEST).send();
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    private <S, T extends Response<S>> S handleError(final T response) {
        if (response.hasError()) {
            throw new EthereumApiException(response.getError().getMessage());
        }
        return response.getResult();
    }

    SmartContractByteCode getCode(EthAddress address) {
        try {
            return SmartContractByteCode.of(web3j.ethGetCode(address.withLeading0x(), DefaultBlockParameterName.LATEST).send().getCode());
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    long getCurrentBlockNumber() {
        try {
            return web3j.ethBlockNumber().send().getBlockNumber().longValue();
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    RawTransaction createTransaction(Nonce nonce, GasPrice gasPrice, GasUsage gasLimit, EthAddress address, EthValue value, EthData data) {
        return RawTransaction.createTransaction(nonce.getValue(), gasPrice.getPrice().inWei(), gasLimit.getUsage(), address.toString(), value.inWei(), data.toString());
    }

    TransactionReceipt getReceipt(EthHash hash) {
        try {
            return handleError(web3j.ethGetTransactionReceipt(hash.withLeading0x()).send());
        } catch (IOException e) {
            throw new EthereumApiException("error while retrieving the transactionReceipt", e);
        }
    }

    org.web3j.protocol.core.methods.response.Transaction getTransaction(EthHash hash) {
        try {
            return handleError(web3j.ethGetTransactionByHash(hash.withLeading0x()).send());
        } catch (IOException e) {
            throw new EthereumApiException("error while retrieving the transactionReceipt", e);
        }
    }

    Optional<EthBlock> getBlock(long blockNumber) {
        try {
            return Optional.ofNullable(web3j.ethGetBlockByNumber(new DefaultBlockParameterNumber(BigInteger.valueOf(blockNumber)), true).send());
        } catch (IOException e) {
            throw new EthereumApiException("error while retrieving the block " + blockNumber, e);
        }
    }

    Optional<EthBlock> getBlock(EthHash blockHash) {
        try {
            return Optional.ofNullable(web3j.ethGetBlockByHash(blockHash.withLeading0x(), true).send());
        } catch (IOException e) {
            throw new EthereumApiException("error while retrieving the block " + blockHash.withLeading0x(), e);
        }
    }
}

package org.adridadou.ethereum.propeller;

import org.adridadou.ethereum.propeller.event.BlockInfo;
import org.adridadou.ethereum.propeller.event.EthereumEventHandler;
import org.adridadou.ethereum.propeller.values.*;

import java.util.Optional;

/**
 * Created by davidroon on 20.01.17.
 * This code is released under Apache 2 license
 */
public interface EthereumBackend {
    GasPrice getGasPrice();

    EthValue getBalance(EthAddress address);

    boolean addressExists(EthAddress address);

    EthHash submit(TransactionRequest request, Nonce nonce);

    EthHash getTransactionHash(TransactionRequest request, Nonce nonce);

    GasUsage estimateGas(EthAccount account, EthAddress address, EthValue value, EthData data);

    Nonce getNonce(EthAddress currentAddress);

    long getCurrentBlockNumber();

    BlockInfo getBlock(long blockNumber);

    BlockInfo getBlock(EthHash blockNumber);

    SmartContractByteCode getCode(EthAddress address);

    EthData constantCall(EthAccount account, EthAddress address, EthValue value, EthData data);

    void register(EthereumEventHandler eventHandler);

    Optional<TransactionInfo> getTransactionInfo(EthHash hash);
}

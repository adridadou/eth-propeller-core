package org.adridadou.ethereum.propeller;

import org.adridadou.ethereum.propeller.event.EthereumEventHandler;
import org.adridadou.ethereum.propeller.values.*;

/**
 * Created by davidroon on 20.01.17.
 * This code is released under Apache 2 license
 */
public interface EthereumBackend {
    GasPrice getGasPrice();

    EthValue getBalance(EthAddress address);

    boolean addressExists(EthAddress address);

    EthHash submit(final EthAccount account, final EthAddress address, final EthValue value, final EthData data, final Nonce nonce, final GasUsage gasLimit);

    GasUsage estimateGas(final EthAccount account, final EthAddress address, final EthValue value, final EthData data);

    Nonce getNonce(EthAddress currentAddress);

    long getCurrentBlockNumber();

    SmartContractByteCode getCode(EthAddress address);

    EthData constantCall(final EthAccount account, final EthAddress address, final EthValue value, final EthData data);

    void register(EthereumEventHandler eventHandler);
}

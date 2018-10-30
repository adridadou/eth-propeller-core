package org.adridadou.ethereum.propeller;

import org.adridadou.ethereum.propeller.event.BlockInfo;
import org.adridadou.ethereum.propeller.event.EthereumEventHandler;
import org.adridadou.ethereum.propeller.solidity.SolidityType;
import org.adridadou.ethereum.propeller.solidity.converters.encoders.NumberEncoder;
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

    GasUsage estimateGas(EthAccount account, EthAddress address, EthValue value, EthData data);

    Nonce getNonce(EthAddress currentAddress);

    long getCurrentBlockNumber();

    Optional<BlockInfo> getBlock(long blockNumber);

    Optional<BlockInfo> getBlock(EthHash blockNumber);

    SmartContractByteCode getCode(EthAddress address);

    EthData constantCall(EthAccount account, EthAddress address, EthValue value, EthData data);

    void register(EthereumEventHandler eventHandler);

    Optional<TransactionInfo> getTransactionInfo(EthHash hash);

    default EthSignature signTransaction(Nonce nonce, GasPrice gasPrice, TransactionRequest request, ChainId chainId) {

        NumberEncoder numberEncoder = new NumberEncoder();
        EthData rawData = numberEncoder.encode(nonce.getValue(), SolidityType.UINT)
                .merge(numberEncoder.encode(gasPrice.getPrice().inWei(), SolidityType.UINT))
                .merge(numberEncoder.encode(request.getGasLimit().getUsage(), SolidityType.UINT))
                .merge(request.getAddress().toData())
                .merge(numberEncoder.encode(request.getValue().inWei(), SolidityType.UINT))
                .merge(request.getData())
                .merge(numberEncoder.encode(chainId.id, SolidityType.UINT))
                .merge(EthData.emptyWord())
                .merge(EthData.emptyWord());


        return request.getAccount().sign(rawData);
    }
}

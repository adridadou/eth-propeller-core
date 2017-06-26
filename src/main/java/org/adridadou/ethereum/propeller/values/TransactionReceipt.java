package org.adridadou.ethereum.propeller.values;

import java.util.List;

/**
 * Created by davidroon on 03.02.17.
 * This code is released under Apache 2 license
 */
public class TransactionReceipt {
    public final EthHash hash;
    public final EthHash blockHash;
    public final EthAddress sender;
    public final EthAddress receiveAddress;
    public final EthAddress contractAddress;
    public final String error;
    public final EthData executionResult;
    public final boolean isSuccessful;
    public final List<EventData> events;

    public TransactionReceipt(EthHash hash, EthHash blockHash, EthAddress sender, EthAddress receiveAddress, EthAddress contractAddress, String error, EthData executionResult, boolean isSuccessful, List<EventData> events) {
        this.hash = hash;
        this.blockHash = blockHash;
        this.sender = sender;
        this.receiveAddress = receiveAddress;
        this.contractAddress = contractAddress;
        this.error = error;
        this.executionResult = executionResult;
        this.isSuccessful = isSuccessful;
        this.events = events;
    }
}

package org.adridadou.ethereum.propeller.event;

import org.adridadou.ethereum.propeller.values.EventInfo;

import java.util.List;

public class OnTransactionParameters {

    public final TransactionReceipt receipt;
    public final TransactionStatus status;
    private final List<EventInfo> logs;
    private final Boolean contractCreation;


    public OnTransactionParameters(TransactionReceipt receipt, TransactionStatus status, List<EventInfo> logs) {
        this.receipt = receipt;
        this.status = status;
        this.logs = logs;
        this.contractCreation = this.receipt != null && this.receipt.receiveAddress.isEmpty();
    }

    public TransactionReceipt getReceipt() {
        return receipt;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public List<EventInfo> getLogs() {
        return logs;
    }

    public Boolean isContractCreation() {
        return contractCreation;
    }
}

package org.adridadou.ethereum.propeller.event;

public class TransactionInfo {

    public final TransactionReceipt receipt;
    public final TransactionStatus status;
    private final Boolean contractCreation;


    public TransactionInfo(TransactionReceipt receipt, TransactionStatus status) {
        this.receipt = receipt;
        this.status = status;
        this.contractCreation = this.receipt != null && this.receipt.receiveAddress.isEmpty();
    }

    public TransactionReceipt getReceipt() {
        return receipt;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public Boolean isContractCreation() {
        return contractCreation;
    }
}

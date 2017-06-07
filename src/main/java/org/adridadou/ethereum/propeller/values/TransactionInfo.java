package org.adridadou.ethereum.propeller.values;

import java.util.Optional;

public class TransactionInfo {

    private final EthHash transactionHash;
    private final TransactionReceipt receipt;
    private final TransactionStatus status;
    private final boolean contractCreation;


    public TransactionInfo(EthHash transactionHash, TransactionReceipt receipt, TransactionStatus status) {
        this.transactionHash = transactionHash;
        this.receipt = receipt;
        this.status = status;
        this.contractCreation = this.receipt != null && Optional.ofNullable(receipt).map(r -> r.receiveAddress.isEmpty()).orElse(false);
    }

    public Optional<TransactionReceipt> getReceipt() {
        return Optional.ofNullable(receipt);
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public EthHash getTransactionHash() {
        return transactionHash;
    }

    public boolean isContractCreation() {
        return contractCreation;
    }
}

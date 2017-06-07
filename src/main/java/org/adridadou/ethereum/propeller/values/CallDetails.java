package org.adridadou.ethereum.propeller.values;

import org.adridadou.ethereum.propeller.event.TransactionReceipt;

import java.util.concurrent.CompletableFuture;

/**
 * Created by davidroon on 06.06.17.
 */
public class CallDetails {
    private final CompletableFuture<TransactionReceipt> result;
    private final EthHash txHash;

    public CallDetails(CompletableFuture<TransactionReceipt> result, EthHash txHash) {
        this.result = result;
        this.txHash = txHash;
    }


    public CompletableFuture<TransactionReceipt> getResult() {
        return result;
    }

    public EthHash getTxHash() {
        return txHash;
    }
}

package org.adridadou.ethereum.propeller.values;

import java.util.concurrent.CompletableFuture;

/**
 * Created by davidroon on 06.06.17.
 */
public class EthCall<T> {

    private final EthHash transactionHash;
    private final CompletableFuture<T> result;

    public EthCall(EthHash transactionHash, CompletableFuture<T> result) {
        this.transactionHash = transactionHash;
        this.result = result;
    }

    public CompletableFuture<T> getResult() {
        return result;
    }

    public EthHash getTransactionHash() {
        return transactionHash;
    }
}

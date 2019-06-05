package org.adridadou.ethereum.propeller.values;

import java.util.concurrent.CompletableFuture;

/**
 * Created by davidroon on 06.06.17.
 */
public class EthCall<T> {

    private final Nonce nonce;
    private final GasUsage gasEstimate;

    private final EthHash transactionHash;
    private final CompletableFuture<T> result;

    public EthCall(Nonce nonce, GasUsage gasEstimate, EthHash transactionHash, CompletableFuture<T> result) {
        this.transactionHash = transactionHash;
        this.result = result;
        this.nonce = nonce;
        this.gasEstimate = gasEstimate;
    }

    public CompletableFuture<T> getResult() {
        return result;
    }

    public EthHash getTransactionHash() {
        return transactionHash;
    }
}

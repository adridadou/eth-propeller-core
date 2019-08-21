package org.adridadou.ethereum.propeller.values;

import java.util.concurrent.CompletableFuture;

/**
 * Created by davidroon on 06.06.17.
 */
public class CallDetails {
    private final CompletableFuture<TransactionReceipt> result;
    private final EthHash txHash;
    private final Nonce nonce;
    private final GasUsage gasEstimate;

    public CallDetails(CompletableFuture<TransactionReceipt> result, EthHash txHash, Nonce nonce, GasUsage gasEstimate) {
        this.result = result;
        this.txHash = txHash;
        this.nonce = nonce;
        this.gasEstimate = gasEstimate;
    }


    public CompletableFuture<TransactionReceipt> getResult() {
        return result;
    }

    public Nonce getNonce() {
        return nonce;
    }

    public GasUsage getGasEstimate() {
        return gasEstimate;
    }

    public EthHash getTxHash() {
        return txHash;
    }
}

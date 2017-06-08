package org.adridadou.ethereum.propeller.values;

/**
 * Created by davidroon on 03.04.17.
 * This code is released under Apache 2 license
 */
public class EventInfo<T> {
    private final EthHash transactionHash;
    private final T result;

    public EventInfo(EthHash transactionHash, T result) {
        this.transactionHash = transactionHash;
        this.result = result;
    }

    public EthHash getTransactionHash() {
        return transactionHash;
    }

    public T getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "EventInfo{" +
                "transactionHash=" + transactionHash +
                ", result=" + result +
                '}';
    }
}

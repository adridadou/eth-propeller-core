package org.adridadou.ethereum.propeller.values;


import org.adridadou.ethereum.propeller.service.CryptoProvider;

import java.util.Objects;

public class TransactionRequest {
    private final CryptoProvider cryptoProvider;
    private final EthAddress address;
    private final EthValue value;
    private final EthData data;
    private final GasUsage gasLimit;
    private final GasPrice gasPrice;

    public TransactionRequest(CryptoProvider cryptoProvider, EthAddress address, EthValue value, EthData data, GasUsage gasLimit, GasPrice gasPrice) {
        this.cryptoProvider = cryptoProvider;
        this.address = address;
        this.value = value;
        this.data = data;
        this.gasLimit = gasLimit;
        this.gasPrice = gasPrice;
    }

    public CryptoProvider getCryptoProvider() {
        return cryptoProvider;
    }

    public EthAddress getAddress() {
        return address;
    }

    public EthValue getValue() {
        return value;
    }

    public EthData getData() {
        return data;
    }

    public GasUsage getGasLimit() {
        return gasLimit;
    }

    public GasPrice getGasPrice() {
        return gasPrice;
    }

    @Override
    public String toString() {
        return "TransactionRequest{" +
                "account=" + cryptoProvider.getAddress().withLeading0x() +
                ", address=" + address +
                ", value=" + value +
                ", data=" + data +
                ", gasLimit=" + gasLimit +
                ", gasPrice=" + gasPrice +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TransactionRequest that = (TransactionRequest) o;

        if (!Objects.equals(cryptoProvider, that.cryptoProvider)) return false;
        if (!Objects.equals(address, that.address)) return false;
        if (!Objects.equals(value, that.value)) return false;
        if (!Objects.equals(data, that.data)) return false;
        if (!Objects.equals(gasLimit, that.gasLimit)) return false;
        return Objects.equals(gasPrice, that.gasPrice);
    }

    @Override
    public int hashCode() {
        int result = cryptoProvider != null ? cryptoProvider.getAddress().hashCode() : 0;
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (data != null ? data.hashCode() : 0);
        result = 31 * result + (gasLimit != null ? gasLimit.hashCode() : 0);
        result = 31 * result + (gasPrice != null ? gasPrice.hashCode() : 0);
        return result;
    }
}

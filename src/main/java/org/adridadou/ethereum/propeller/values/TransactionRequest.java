package org.adridadou.ethereum.propeller.values;


public class TransactionRequest {
    private final EthAccount account;
    private final EthAddress address;
    private final EthValue value;
    private final EthData data;
    private final GasUsage gasLimit;
    private final GasPrice gasPrice;

    public TransactionRequest(EthAccount account, EthAddress address, EthValue value, EthData data, GasUsage gasLimit, GasPrice gasPrice) {
        this.account = account;
        this.address = address;
        this.value = value;
        this.data = data;
        this.gasLimit = gasLimit;
        this.gasPrice = gasPrice;
    }

    public EthAccount getAccount() {
        return account;
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
                "account=" + account +
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

        if (account != null ? !account.equals(that.account) : that.account != null) return false;
        if (address != null ? !address.equals(that.address) : that.address != null) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;
        if (data != null ? !data.equals(that.data) : that.data != null) return false;
        if (gasLimit != null ? !gasLimit.equals(that.gasLimit) : that.gasLimit != null) return false;
        return gasPrice != null ? gasPrice.equals(that.gasPrice) : that.gasPrice == null;
    }

    @Override
    public int hashCode() {
        int result = account != null ? account.hashCode() : 0;
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (data != null ? data.hashCode() : 0);
        result = 31 * result + (gasLimit != null ? gasLimit.hashCode() : 0);
        result = 31 * result + (gasPrice != null ? gasPrice.hashCode() : 0);
        return result;
    }
}

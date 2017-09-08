package org.adridadou.ethereum.propeller.values;

/**
 * Created by davidroon on 03.04.17.
 * This code is released under Apache 2 license
 */
public class GasPrice {
    private final EthValue price;

    public GasPrice(EthValue price) {
        this.price = price;
    }

    public EthValue getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return "GasPrice{" +
                "price=" + price +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GasPrice gasPrice = (GasPrice) o;

        return price != null ? price.equals(gasPrice.price) : gasPrice.price == null;
    }

    @Override
    public int hashCode() {
        return price != null ? price.hashCode() : 0;
    }
}

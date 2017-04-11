package org.adridadou.ethereum.propeller.values;

import java.math.BigInteger;

/**
 * Created by davidroon on 03.04.17.
 * This code is released under Apache 2 license
 */
public class GasPrice {
    private final BigInteger price;

    public GasPrice(BigInteger price) {
        this.price = price;
    }

    public BigInteger getPrice() {
        return price;
    }
}

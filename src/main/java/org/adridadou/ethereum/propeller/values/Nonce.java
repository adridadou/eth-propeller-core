package org.adridadou.ethereum.propeller.values;

import java.math.BigInteger;

/**
 * Created by davidroon on 03.04.17.
 * This code is released under Apache 2 license
 */
public class Nonce {
    private final BigInteger value;

    public Nonce(BigInteger value) {
        this.value = value;
    }

    public BigInteger getValue() {
        return value;
    }

    public Nonce add(Integer offset) {
        return new Nonce(this.value.add(BigInteger.valueOf(offset)));
    }
}

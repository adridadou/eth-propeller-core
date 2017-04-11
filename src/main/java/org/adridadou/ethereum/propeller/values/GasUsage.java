package org.adridadou.ethereum.propeller.values;

import java.math.BigInteger;

/**
 * Created by davidroon on 03.04.17.
 * This code is released under Apache 2 license
 */
public class GasUsage {
    private final BigInteger usage;

    public GasUsage(BigInteger usage) {
        this.usage = usage;
    }

    public BigInteger getUsage() {
        return usage;
    }

    public GasUsage add(int additionalGasForContractCreation) {
        return new GasUsage(this.usage.add(BigInteger.valueOf(additionalGasForContractCreation)));
    }
}

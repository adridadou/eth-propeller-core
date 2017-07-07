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

    @Override
    public String toString() {
        return "GasUsage{" +
                "usage=" + usage +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GasUsage gasUsage = (GasUsage) o;

        return usage != null ? usage.equals(gasUsage.usage) : gasUsage.usage == null;
    }

    @Override
    public int hashCode() {
        return usage != null ? usage.hashCode() : 0;
    }
}

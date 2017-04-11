package org.adridadou.ethereum.propeller.solidity.converters.encoders;

import org.adridadou.ethereum.propeller.values.EthData;

import java.math.BigInteger;

/**
 * Created by davidroon on 03.04.17.
 * This code is released under Apache 2 license
 */
public class NumberEncoder implements SolidityTypeEncoder {

    @Override
    public boolean canConvert(Class<?> type) {
        return type.getTypeName().equals("int") || type.getTypeName().equals("long") || Number.class.isAssignableFrom(type);
    }

    @Override
    public EthData encode(Object arg) {
        if (arg instanceof BigInteger) {
            return EthData.of((BigInteger) arg);
        }
        return EthData.of(((Number) arg).longValue());
    }
}

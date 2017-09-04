package org.adridadou.ethereum.propeller.solidity.converters.encoders;

import org.adridadou.ethereum.propeller.exception.EthereumApiException;
import org.adridadou.ethereum.propeller.solidity.SolidityType;
import org.adridadou.ethereum.propeller.values.EthData;

import java.math.BigDecimal;
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
    public EthData encode(Object arg, SolidityType solidityType) {
        if (solidityType.name().startsWith("U")) {
            if (arg instanceof BigInteger) {
                //we can only accept non decimal values
                if (((BigInteger) arg).signum() == -1) {
                    throw new EthereumApiException("unsigned type cannot encode negative values");
                }
            } else if (arg instanceof BigDecimal) {
                return encode(((BigDecimal) arg).toBigInteger(), solidityType);
            } else if (((Number) arg).longValue() < 0) {
                throw new EthereumApiException("unsigned type cannot encode negative values");
            }
        }
        if (arg instanceof BigInteger) {
            return EthData.of((BigInteger) arg);
        }
        return EthData.of(((Number) arg).longValue());
    }
}

package org.adridadou.ethereum.propeller.solidity.converters.encoders;

import org.adridadou.ethereum.propeller.exception.EthereumApiException;
import org.adridadou.ethereum.propeller.values.EthData;

import java.math.BigInteger;

/**
 * Created by davidroon on 03.04.17.
 * This code is released under Apache 2 license
 */
public class BooleanEncoder implements SolidityTypeEncoder {
    @Override
    public boolean canConvert(Class<?> type) {
        return type.equals(Boolean.class);
    }

    @Override
    public EthData encode(Object arg) {
        if (arg instanceof Boolean) {
            return EthData.of((Boolean) arg ? BigInteger.ONE : BigInteger.ZERO);
        }
        throw new EthereumApiException("cannot encode bool value:" + arg);
    }
}

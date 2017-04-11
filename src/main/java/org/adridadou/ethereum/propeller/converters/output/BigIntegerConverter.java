package org.adridadou.ethereum.propeller.converters.output;

import org.adridadou.ethereum.propeller.exception.EthereumApiException;

import java.lang.reflect.Type;
import java.math.BigInteger;

/**
 * Created by davidroon on 05.01.17.
 * This code is released under Apache 2 license
 */
public class BigIntegerConverter implements OutputTypeConverter {
    @Override
    public boolean isOfType(Class<?> cls) {
        return BigInteger.class.equals(cls);
    }

    @Override
    public BigInteger convert(Object obj, Type genericType) {
        if (obj == null || obj instanceof BigInteger) {
            return (BigInteger) obj;
        }
        throw new EthereumApiException("cannot convert " + obj.getClass().getSimpleName() + " to BigInteger");
    }
}

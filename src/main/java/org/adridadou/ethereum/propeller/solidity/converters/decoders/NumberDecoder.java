package org.adridadou.ethereum.propeller.solidity.converters.decoders;

import org.adridadou.ethereum.propeller.exception.EthereumApiException;
import org.adridadou.ethereum.propeller.values.EthData;

import java.lang.reflect.Type;
import java.math.BigInteger;

import static org.adridadou.ethereum.propeller.values.EthData.WORD_SIZE;

/**
 * Created by davidroon on 03.04.17.
 * This code is released under Apache 2 license
 */
public class NumberDecoder implements SolidityTypeDecoder {

    @Override
    public Number decode(Integer index, EthData data, Type resultType) {
        EthData word = data.word(index);
        if (word.length() > WORD_SIZE) {
            throw new EthereumApiException("a word should be of size 32:" + word.length());
        }
        BigInteger number = (word.isEmpty() ? BigInteger.ZERO : new BigInteger(word.data));
        Class<?> resultCls = (Class) resultType;

        if (resultCls.getTypeName().equals("long")) {
            return number.longValueExact();
        }

        if (resultCls.getTypeName().equals("int")) {
            return number.intValueExact();
        }

        if (resultCls.equals(Long.class)) {
            return number.longValueExact();
        }

        if (resultCls.equals(Integer.class)) {
            return number.intValueExact();
        }

        if (resultCls.equals(Short.class)) {
            return number.shortValueExact();
        }

        if (resultCls.equals(Byte.class)) {
            return number.byteValueExact();
        }

        if (resultCls.equals(BigInteger.class)) {
            return number;
        }

        throw new EthereumApiException("cannot convert to " + resultCls.getName());
    }

    @Override
    public boolean canDecode(Class<?> resultCls) {
        return resultCls.getTypeName().equals("int") || resultCls.getTypeName().equals("long") || Number.class.isAssignableFrom(resultCls);
    }
}

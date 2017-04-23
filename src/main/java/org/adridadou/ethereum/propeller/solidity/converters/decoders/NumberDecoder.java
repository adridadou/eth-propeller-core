package org.adridadou.ethereum.propeller.solidity.converters.decoders;

import org.adridadou.ethereum.propeller.exception.EthereumApiException;
import org.adridadou.ethereum.propeller.util.CastUtil;
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
        BigInteger number = word.isEmpty() ? BigInteger.ZERO : new BigInteger(word.data);

        return CastUtil.<Number>matcher()
                .typeNameEquals("long", number::longValueExact)
                .typeNameEquals("int", number::intValueExact)
                .typeNameEquals("short", number::shortValueExact)
                .typeNameEquals("byte", number::byteValueExact)
                .typeEquals(Long.class, number::longValueExact)
                .typeEquals(Integer.class, number::intValueExact)
                .typeEquals(Short.class, number::shortValueExact)
                .typeEquals(Byte.class, number::byteValueExact)
                .typeEquals(BigInteger.class, () -> number)
                .orElseThrowWithErrorMessage("cannot convert to " + resultType.getTypeName())
                .matches((Class<? extends Number>) resultType);
    }

    @Override
    public boolean canDecode(Class<?> resultCls) {
        return resultCls.getTypeName().equals("int") || resultCls.getTypeName().equals("long") || Number.class.isAssignableFrom(resultCls);
    }
}

package org.adridadou.ethereum.propeller.solidity.converters.decoders;

import org.adridadou.ethereum.propeller.exception.EthereumApiException;
import org.adridadou.ethereum.propeller.values.EthData;
import org.adridadou.ethereum.propeller.values.EthValue;

import java.lang.reflect.Type;
import java.math.BigInteger;

import static org.adridadou.ethereum.propeller.values.EthData.WORD_SIZE;

/**
 * Created by davidroon on 03.04.17.
 * This code is released under Apache 2 license
 */
public class EthValueDecoder implements SolidityTypeDecoder {

    @Override
    public EthValue decode(Integer index, EthData data, Type resultType) {
        EthData word = data.word(index);
        if (word.length() > WORD_SIZE) {
            throw new EthereumApiException("a word should be of size 32:" + word.length());
        }
        return EthValue.wei(word.isEmpty() ? BigInteger.ZERO : new BigInteger(word.data));

    }

    @Override
    public boolean canDecode(Class<?> resultCls) {
        return EthValue.class.equals(resultCls);
    }
}

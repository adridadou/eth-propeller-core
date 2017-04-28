package org.adridadou.ethereum.propeller.solidity.converters.decoders;

import org.adridadou.ethereum.propeller.values.EthData;

import java.lang.reflect.Type;
import java.math.BigInteger;

/**
 * Created by davidroon on 03.04.17.
 * This code is released under Apache 2 license
 */
public class BooleanDecoder implements SolidityTypeDecoder {

    @Override
    public Boolean decode(Integer index, EthData data, Type resultType) {
        EthData word = data.word(index);
        return !(word.isEmpty() || (new BigInteger(1, word.data)).equals(BigInteger.ZERO));
    }

    @Override
    public boolean canDecode(Class<?> resultCls) {
        return Boolean.class.equals(resultCls) || resultCls.getName().equals("boolean");
    }
}

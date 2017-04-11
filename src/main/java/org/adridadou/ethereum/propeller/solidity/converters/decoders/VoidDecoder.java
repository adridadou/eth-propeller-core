package org.adridadou.ethereum.propeller.solidity.converters.decoders;

import org.adridadou.ethereum.propeller.values.EthData;

import java.lang.reflect.Type;

/**
 * Created by davidroon on 03.04.17.
 * This code is released under Apache 2 license
 */
public class VoidDecoder implements SolidityTypeDecoder {

    @Override
    public Void decode(EthData word, EthData data, Type resultType) {
        return null;
    }

    @Override
    public boolean canDecode(Class<?> resultCls) {
        return Void.class.equals(resultCls) || resultCls.getName().equals("void");
    }
}

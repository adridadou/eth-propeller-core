package org.adridadou.ethereum.propeller.solidity.converters.decoders;

import org.adridadou.ethereum.propeller.values.EthData;

import java.lang.reflect.Type;

/**
 * Created by davidroon on 23.04.17.
 * This code is released under Apache 2 license
 */
public class EthDataDecoder implements SolidityTypeDecoder {
    @Override
    public EthData decode(Integer index, EthData data, Type resultType) {
        return data.word(index);
    }

    @Override
    public boolean canDecode(Class<?> resultCls) {
        return EthData.class.equals(resultCls);
    }
}

package org.adridadou.ethereum.propeller.solidity.converters.decoders;

import org.adridadou.ethereum.propeller.values.EthData;

import java.lang.reflect.Type;

/**
 * Created by davidroon on 02.04.17.
 * This code is released under Apache 2 license
 */
public interface SolidityTypeDecoder {
    Object decode(Integer index, EthData data, Type resultType);

    boolean canDecode(Class<?> resultCls);
}

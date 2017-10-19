package org.adridadou.ethereum.propeller.solidity.converters.decoders.list;

import org.adridadou.ethereum.propeller.solidity.converters.decoders.SolidityTypeDecoder;
import org.adridadou.ethereum.propeller.values.EthData;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by davidroon on 04.04.17.
 * This code is released under Apache 2 license
 */
public class ArrayDecoder extends CollectionDecoder {

    public ArrayDecoder(List<SolidityTypeDecoder> decoders, Integer size) {
        super(decoders, size);
    }

    @Override
    public Object[] decode(Integer index, EthData data, Type resultType) {
        return decodeCollection(index, data, ((Class) resultType).getComponentType());
    }

    @Override
    public boolean canDecode(Class<?> resultCls) {
        return resultCls.isArray();
    }
}

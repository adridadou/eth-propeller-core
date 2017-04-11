package org.adridadou.ethereum.propeller.solidity.converters.decoders.list;

import org.adridadou.ethereum.propeller.solidity.converters.decoders.SolidityTypeDecoder;
import org.adridadou.ethereum.propeller.values.EthData;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by davidroon on 04.04.17.
 * This code is released under Apache 2 license
 */
public class SetDecoder extends CollectionDecoder {

    public SetDecoder(List<SolidityTypeDecoder> decoders, Integer size) {
        super(decoders, size);
    }

    @Override
    public Object decode(EthData word, EthData data, Type resultType) {
        return new HashSet<>(Arrays.asList(decodeCollection(word, data, getGenericType(resultType))));
    }

    @Override
    public boolean canDecode(Class<?> resultCls) {
        return resultCls.equals(Set.class);
    }
}

package org.adridadou.ethereum.propeller.solidity.converters.decoders.list;

import org.adridadou.ethereum.propeller.exception.EthereumApiException;
import org.adridadou.ethereum.propeller.solidity.converters.decoders.SolidityTypeDecoder;
import org.adridadou.ethereum.propeller.values.EthData;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by davidroon on 04.04.17.
 * This code is released under Apache 2 license
 */
public abstract class CollectionDecoder implements SolidityTypeDecoder {
    private final List<SolidityTypeDecoder> decoders;
    private final int size;

    CollectionDecoder(List<SolidityTypeDecoder> decoders, Integer size) {
        this.decoders = decoders;
        this.size = size;
    }

    Object[] decodeCollection(Integer index, EthData data, Class<?> subResultType) {
        SolidityTypeDecoder decoder = decoders.stream()
                .filter(dec -> dec.canDecode(subResultType)).findFirst()
                .orElseThrow(() -> new EthereumApiException("no decoder found. serious bug detected!"));


        Object[] result = (Object[]) Array.newInstance(subResultType, this.size);
        for (int i = 0; i < this.size; ++i) {
            result[i] = decoder.decode(i + index, data, subResultType);
        }
        return result;
    }

    Class<?> getGenericType(Type genericType) {
        return (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[0];
    }
}

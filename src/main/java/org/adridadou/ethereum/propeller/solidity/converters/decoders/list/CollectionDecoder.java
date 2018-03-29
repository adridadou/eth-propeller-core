package org.adridadou.ethereum.propeller.solidity.converters.decoders.list;

import org.adridadou.ethereum.propeller.exception.EthereumApiException;
import org.adridadou.ethereum.propeller.solidity.converters.decoders.NumberDecoder;
import org.adridadou.ethereum.propeller.solidity.converters.decoders.SolidityTypeDecoder;
import org.adridadou.ethereum.propeller.values.EthData;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import static org.adridadou.ethereum.propeller.values.EthData.WORD_SIZE;

/**
 * Created by davidroon on 04.04.17.
 * This code is released under Apache 2 license
 */
public abstract class CollectionDecoder implements SolidityTypeDecoder {
    private final List<SolidityTypeDecoder> decoders;
    private final int size;
    private final boolean isDynamic;
    private final NumberDecoder numberDecoder = new NumberDecoder();

    CollectionDecoder(List<SolidityTypeDecoder> decoders, Integer size) {
        this.decoders = decoders;
        this.size = size;
        this.isDynamic = false;
    }

    CollectionDecoder(List<SolidityTypeDecoder> decoders) {
        this.decoders = decoders;
        this.size = 0;
        this.isDynamic = true;
    }

    Object[] decodeCollection(Integer index, EthData data, Class<?> subResultType) {
        SolidityTypeDecoder decoder = decoders.stream()
                .filter(dec -> dec.canDecode(subResultType)).findFirst()
                .orElseThrow(() -> new EthereumApiException("no decoder found. serious bug detected!"));
        int size = this.size;
        int arrIndex = index;

        if (this.isDynamic) {
            arrIndex = (numberDecoder.decode(index, data, Integer.class).intValue() / WORD_SIZE);
            size = numberDecoder.decode(arrIndex, data, Integer.class).intValue();
            arrIndex++;
        }

        Object[] result = (Object[]) Array.newInstance(subResultType, size);
        for (int i = 0; i < size; ++i) {
            result[i] = decoder.decode(i + arrIndex, data, subResultType);
        }
        return result;
    }

    Class<?> getGenericType(Type genericType) {
        return (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[0];
    }
}

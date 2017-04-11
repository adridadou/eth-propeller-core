package org.adridadou.ethereum.propeller.solidity.converters.decoders.list;

import org.adridadou.ethereum.propeller.exception.EthereumApiException;
import org.adridadou.ethereum.propeller.solidity.converters.decoders.NumberDecoder;
import org.adridadou.ethereum.propeller.solidity.converters.decoders.SolidityTypeDecoder;
import org.adridadou.ethereum.propeller.values.EthData;
import org.apache.commons.lang.ArrayUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import static org.adridadou.ethereum.propeller.values.EthData.WORD_SIZE;

/**
 * Created by davidroon on 04.04.17.
 * This code is released under Apache 2 license
 */
public abstract class CollectionDecoder implements SolidityTypeDecoder {

    private final NumberDecoder numberDecoder = new NumberDecoder();
    private final List<SolidityTypeDecoder> decoders;
    private final int size;

    public CollectionDecoder(List<SolidityTypeDecoder> decoders, Integer size) {
        this.decoders = decoders;
        this.size = size;
    }

    public Object[] decodeCollection(EthData word, EthData data, Class<?> subResultType) {
        Integer pos = numberDecoder.decode(word, data, Integer.class).intValue();

        SolidityTypeDecoder decoder = decoders.stream()
                .filter(dec -> dec.canDecode(subResultType)).findFirst()
                .orElseThrow(() -> new EthereumApiException("no decoder found. serious bug detected!"));

        Object[] result = new Object[this.size];
        EthData collectionData = EthData.of(ArrayUtils.subarray(data.data, pos, pos + WORD_SIZE * size));
        for (int i = 0; i < this.size; ++i) {
            result[i] = decoder.decode(collectionData.word(i), data, subResultType);
        }
        return result;
    }

    protected Class<?> getGenericType(Type genericType) {
        return (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[0];
    }
}

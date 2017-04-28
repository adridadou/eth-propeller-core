package org.adridadou.ethereum.propeller.solidity.converters.decoders.list;

import org.adridadou.ethereum.propeller.solidity.converters.decoders.NumberDecoder;
import org.adridadou.ethereum.propeller.solidity.converters.decoders.SolidityTypeDecoder;
import org.adridadou.ethereum.propeller.values.EthData;
import org.apache.commons.lang.ArrayUtils;

import java.lang.reflect.Type;
import java.util.List;

import static org.adridadou.ethereum.propeller.values.EthData.WORD_SIZE;

/**
 * Created by davidroon on 04.04.17.
 * This code is released under Apache 2 license
 */
public class EthDataListDecoder extends CollectionDecoder {

    private final NumberDecoder decoder = new NumberDecoder();

    public EthDataListDecoder(List<SolidityTypeDecoder> decoders, Integer size) {
        super(decoders, size);
    }

    @Override
    public Object decode(Integer index, EthData data, Type resultType) {
        Integer strIndex = decoder.decode(index, data, Integer.class).intValue() / WORD_SIZE;
        Integer len = decoder.decode(strIndex, data, Integer.class).intValue();
        return EthData.of(ArrayUtils.subarray(data.data, (strIndex + 1) * WORD_SIZE, (strIndex + 1) * WORD_SIZE + len));
    }

    @Override
    public boolean canDecode(Class<?> resultCls) {
        return EthData.class.equals(resultCls);
    }
}

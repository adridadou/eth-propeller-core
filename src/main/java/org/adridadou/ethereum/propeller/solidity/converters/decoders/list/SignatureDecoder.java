package org.adridadou.ethereum.propeller.solidity.converters.decoders.list;

import org.adridadou.ethereum.propeller.solidity.converters.decoders.NumberDecoder;
import org.adridadou.ethereum.propeller.solidity.converters.decoders.SolidityTypeDecoder;
import org.adridadou.ethereum.propeller.values.EthData;
import org.adridadou.ethereum.propeller.values.EthSignature;
import org.apache.commons.lang.ArrayUtils;

import java.lang.reflect.Type;
import java.util.List;

import static org.adridadou.ethereum.propeller.values.EthData.WORD_SIZE;

/**
 * Created by davidroon on 23.04.17.
 * This code is released under Apache 2 license
 */
public class SignatureDecoder extends CollectionDecoder {

    private final NumberDecoder decoder = new NumberDecoder();

    public SignatureDecoder(List<SolidityTypeDecoder> decoders) {
        super(decoders);
    }

    public SignatureDecoder(List<SolidityTypeDecoder> decoders, Integer size) {
        super(decoders, size);
    }

    @Override
    public EthSignature decode(Integer index, EthData data, Type resultType) {
        Integer strIndex = decoder.decode(index, data, Integer.class).intValue() / WORD_SIZE;
        Integer len = decoder.decode(strIndex, data, Integer.class).intValue();
        return EthSignature.of(ArrayUtils.subarray(data.data, (strIndex + 1) * WORD_SIZE, (strIndex + 1) * WORD_SIZE + len));
    }

    @Override
    public boolean canDecode(Class<?> resultCls) {
        return EthSignature.class.equals(resultCls);
    }
}

package org.adridadou.ethereum.propeller.solidity.converters.encoders.list;

import org.adridadou.ethereum.propeller.exception.EthereumApiException;
import org.adridadou.ethereum.propeller.solidity.SolidityType;
import org.adridadou.ethereum.propeller.solidity.converters.encoders.NumberEncoder;
import org.adridadou.ethereum.propeller.solidity.converters.encoders.SolidityTypeEncoder;
import org.adridadou.ethereum.propeller.values.EthData;

import java.util.List;

/**
 * Created by davidroon on 04.04.17.
 * This code is released under Apache 2 license
 */
public abstract class CollectionEncoder implements SolidityTypeEncoder {
    private final NumberEncoder numberEncoder = new NumberEncoder();
    private final List<SolidityTypeEncoder> encoders;
    private final int size;
    private final boolean isDynamic;

    CollectionEncoder(List<SolidityTypeEncoder> encoders) {
        this.encoders = encoders;
        this.size = 0;
        this.isDynamic = true;
    }

    CollectionEncoder(List<SolidityTypeEncoder> encoders, Integer size) {
        this.encoders = encoders;
        this.size = size;
        this.isDynamic = false;
    }

    public EthData encode(List<?> lst, SolidityType solidityType) {
        if (isDynamic) {
            return encodeDynamicSizeList(lst, solidityType);
        }
        return encodeFixedSizeList(lst, solidityType);
    }

    private EthData encodeDynamicSizeList(List<?> lst, SolidityType solidityType) {
        EthData result = lst.stream()
                .map(entry -> encoders.stream()
                        .filter(enc -> enc.canConvert(entry.getClass()))
                        .findFirst().orElseThrow(() -> new EthereumApiException("no encoder found for list entry"))
                        .encode(entry, solidityType))
                .reduce(EthData.empty(), EthData::merge);

        return numberEncoder.encode(lst.size(), SolidityType.UINT).merge(result);
    }

    private EthData encodeFixedSizeList(List<?> lst, SolidityType solidityType) {
        int diff = size - lst.size();
        if (diff < 0) {
            throw new EthereumApiException("List size (" + lst.size() + ") != " + size + " for type " + solidityType.name() + "[" + size + "]");
        }
        EthData result = lst.stream()
                .map(entry -> encoders.stream()
                        .filter(enc -> enc.canConvert(entry.getClass()))
                        .findFirst().orElseThrow(() -> new EthereumApiException("no encoder found for list entry"))
                        .encode(entry, solidityType))
                .reduce(EthData.empty(), EthData::merge);

        for (int i = 0; i < diff; i++) {
            result = result.merge(EthData.emptyWord());
        }

        return result;
    }
}

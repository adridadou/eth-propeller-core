package org.adridadou.ethereum.propeller.solidity.converters.encoders.list;

import org.adridadou.ethereum.propeller.exception.EthereumApiException;
import org.adridadou.ethereum.propeller.solidity.SolidityType;
import org.adridadou.ethereum.propeller.solidity.converters.encoders.SolidityTypeEncoder;
import org.adridadou.ethereum.propeller.values.EthData;

import java.util.List;

/**
 * Created by davidroon on 04.04.17.
 * This code is released under Apache 2 license
 */
public abstract class CollectionEncoder implements SolidityTypeEncoder {
    private final List<SolidityTypeEncoder> encoders;
    private final int size;

    CollectionEncoder(List<SolidityTypeEncoder> encoders, Integer size) {
        this.encoders = encoders;
        this.size = size;
    }

    public EthData encode(List<?> lst, SolidityType solidityType) {
        int diff = size - lst.size();
        if (diff < 0)
            throw new RuntimeException("List size (" + lst.size() + ") != " + size + " for type " + solidityType.name() + "[" + size + "]");
        EthData result = lst.stream()
                .map(entry -> {
                    SolidityTypeEncoder encoder = encoders.stream()
                            .filter(enc -> enc.canConvert(entry.getClass())).findFirst()
                            .orElseThrow(() -> new EthereumApiException("no encoder found for list entry"));

                    return encoder.encode(entry, solidityType);
                })
                .reduce(EthData.empty(), EthData::merge);

        for (int i = 0; i < diff; i++) {
            result = result.merge(EthData.emptyWord());
        }

        return result;
    }
}

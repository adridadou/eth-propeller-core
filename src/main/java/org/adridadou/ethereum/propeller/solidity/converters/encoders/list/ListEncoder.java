package org.adridadou.ethereum.propeller.solidity.converters.encoders.list;

import org.adridadou.ethereum.propeller.solidity.SolidityType;
import org.adridadou.ethereum.propeller.solidity.converters.encoders.SolidityTypeEncoder;
import org.adridadou.ethereum.propeller.values.EthData;

import java.util.List;

/**
 * Created by davidroon on 14.04.17.
 * This code is released under Apache 2 license
 */
public class ListEncoder extends CollectionEncoder {

    public ListEncoder(List<SolidityTypeEncoder> encoders) {
        super(encoders);
    }

    public ListEncoder(List<SolidityTypeEncoder> encoders, Integer size) {
        super(encoders, size);
    }

    @Override
    public boolean canConvert(Class<?> type) {
        return List.class.isAssignableFrom(type);
    }

    @Override
    public EthData encode(Object arg, SolidityType solidityType) {
        return encode((List) arg, solidityType);
    }


}

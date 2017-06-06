package org.adridadou.ethereum.propeller.solidity.converters.encoders;

import org.adridadou.ethereum.propeller.solidity.SolidityType;
import org.adridadou.ethereum.propeller.values.EthData;
import org.adridadou.ethereum.propeller.values.EthValue;

/**
 * Created by davidroon on 03.04.17.
 * This code is released under Apache 2 license
 */
public class EthValueEncoder implements SolidityTypeEncoder {

    @Override
    public boolean canConvert(Class<?> type) {
        return EthValue.class.equals(type);
    }

    @Override
    public EthData encode(Object arg, SolidityType solidityType) {
        EthValue value = (EthValue) arg;
        return EthData.of(value.inWei());
    }
}

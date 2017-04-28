package org.adridadou.ethereum.propeller.solidity.converters.encoders;

import org.adridadou.ethereum.propeller.solidity.SolidityType;
import org.adridadou.ethereum.propeller.values.EthData;

/**
 * Created by davidroon on 08.04.17.
 * This code is released under Apache 2 license
 */
public class EnumEncoder implements SolidityTypeEncoder {

    private final NumberEncoder numberEncoder = new NumberEncoder();

    @Override
    public boolean canConvert(Class<?> type) {
        return type.isEnum();
    }

    @Override
    public EthData encode(Object arg, SolidityType solidityType) {
        return numberEncoder.encode(((Enum) arg).ordinal(), SolidityType.UINT);
    }
}

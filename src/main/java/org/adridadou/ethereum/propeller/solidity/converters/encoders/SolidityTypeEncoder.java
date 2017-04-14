package org.adridadou.ethereum.propeller.solidity.converters.encoders;


import org.adridadou.ethereum.propeller.solidity.SolidityType;
import org.adridadou.ethereum.propeller.values.EthData;

/**
 * Created by davidroon on 02.04.17.
 * This code is released under Apache 2 license
 */
public interface SolidityTypeEncoder {
    boolean canConvert(Class<?> type);

    EthData encode(Object arg, SolidityType solidityType);

}

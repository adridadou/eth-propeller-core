package org.adridadou.ethereum.propeller.solidity.converters.encoders;

import org.adridadou.ethereum.propeller.solidity.SolidityType;
import org.adridadou.ethereum.propeller.values.EthData;
import org.adridadou.ethereum.propeller.values.EthSignature;

/**
 * Created by davidroon on 05.04.17.
 * This code is released under Apache 2 license
 */
public class SignatureEncoder implements SolidityTypeEncoder {

    private final EthDataEncoder dataEncoder = new EthDataEncoder();

    @Override
    public boolean canConvert(Class<?> type) {
        return EthSignature.class.equals(type);
    }

    @Override
    public EthData encode(Object arg, SolidityType solidityType) {
        return dataEncoder.encode(((EthSignature) arg).toData(), SolidityType.BYTES);
    }
}

package org.adridadou.ethereum.propeller.solidity.converters.encoders;

import org.adridadou.ethereum.propeller.values.EthAddress;
import org.adridadou.ethereum.propeller.values.EthData;

import java.math.BigInteger;

/**
 * Created by davidroon on 05.04.17.
 * This code is released under Apache 2 license
 */
public class AddressEncoder implements SolidityTypeEncoder {
    private final NumberEncoder bigIntegerEncoder = new NumberEncoder();

    @Override
    public boolean canConvert(Class<?> type) {
        return EthAddress.class.equals(type);
    }

    @Override
    public EthData encode(Object arg) {
        return bigIntegerEncoder.encode(new BigInteger(1, ((EthAddress) arg).address));
    }
}

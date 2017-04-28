package org.adridadou.ethereum.propeller.solidity.converters.encoders;

import org.adridadou.ethereum.propeller.solidity.SolidityType;
import org.adridadou.ethereum.propeller.values.EthData;

import java.util.Date;

/**
 * Created by davidroon on 05.04.17.
 * This code is released under Apache 2 license
 */
public class DateEncoder implements SolidityTypeEncoder {
    private final NumberEncoder numberEncoder = new NumberEncoder();

    @Override
    public boolean canConvert(Class<?> type) {
        return Date.class.equals(type);
    }

    @Override
    public EthData encode(Object arg, SolidityType solidityType) {
        Date date = (Date) arg;
        return numberEncoder.encode(date.getTime(), solidityType);
    }
}

package org.adridadou.ethereum.propeller.solidity.converters.encoders;

import org.adridadou.ethereum.propeller.solidity.SolidityType;
import org.adridadou.ethereum.propeller.values.EthData;

import java.nio.charset.StandardCharsets;

/**
 * Created by davidroon on 04.04.17.
 * This code is released under Apache 2 license
 */
public class StringEncoder implements SolidityTypeEncoder {
    @Override
    public boolean canConvert(Class<?> type) {
        return String.class.equals(type);
    }

    public EthData encode(Object value, SolidityType solidityType) {
        String str = (String) value;
        byte[] bytesValue = str.getBytes(StandardCharsets.UTF_8);
        byte[] resizedBytesValue = new byte[(((bytesValue.length - 1) / 32) + 1) * 32];
        System.arraycopy(bytesValue, 0, resizedBytesValue, 0, bytesValue.length);

        return EthData.of(bytesValue.length).merge(EthData.of(resizedBytesValue));
    }
}

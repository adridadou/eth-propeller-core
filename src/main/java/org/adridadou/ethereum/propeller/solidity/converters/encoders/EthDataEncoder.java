package org.adridadou.ethereum.propeller.solidity.converters.encoders;

import org.adridadou.ethereum.propeller.exception.EthereumApiException;
import org.adridadou.ethereum.propeller.solidity.SolidityType;
import org.adridadou.ethereum.propeller.values.EthData;

/**
 * Created by davidroon on 23.04.17.
 * This code is released under Apache 2 license
 */
public class EthDataEncoder implements SolidityTypeEncoder {
    private final NumberEncoder numberEncoder = new NumberEncoder();

    @Override
    public boolean canConvert(Class<?> type) {
        return EthData.class.equals(type);
    }

    @Override
    public EthData encode(Object arg, SolidityType solidityType) {
        switch (solidityType) {
            case BYTES:
                return encodeToBytes((EthData) arg);
            case BYTES8:
            case BYTES16:
            case BYTES32:
                return encodeToBytes32((EthData) arg);
            default:
                throw new EthereumApiException("EthData can be encoded to Bytes and Bytes32 only");
        }
    }

    private EthData encodeToBytes32(EthData arg) {
        if (arg.length() > EthData.WORD_SIZE) {
            throw new EthereumApiException("bytes32 is a word only, EthData data too big " + arg.length());
        }
        return arg.word(0);
    }

    private EthData encodeToBytes(EthData arg) {
        EthData lengthData = numberEncoder.encode(arg.length(), SolidityType.UINT);
        return lengthData.merge(arg);
    }
}

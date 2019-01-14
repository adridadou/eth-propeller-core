package org.adridadou.ethereum.propeller.values;

import org.adridadou.ethereum.propeller.exception.EthereumApiException;
import org.spongycastle.util.encoders.Hex;

import java.util.Arrays;

/**
 * Created by davidroon on 19.04.16.
 * This code is released under Apache 2 license
 */
public final class EthAddress {
    public static final int MAX_ADDRESS_SIZE = 20;
    private static final byte[] EMPTY_ARRAY = new byte[0];
    public final byte[] address;

    private EthAddress(byte[] address) {
        if (address.length > MAX_ADDRESS_SIZE) {
            throw new EthereumApiException("byte array of the address cannot be bigger than 20.value:" + Hex.toHexString(address));
        }
        this.address = address;
    }

    public static EthAddress of(byte[] address) {
        if (address == null) {
            return EthAddress.empty();
        }

        return new EthAddress(trimLeft(address));
    }

    public static EthAddress of(final String address) {
        if (address == null) {
            return empty();
        }
        if (address.startsWith("0x")) {
            return of(Hex.decode(address.substring(2)));
        }
        return of(Hex.decode(address));
    }

    public static byte[] trimLeft(byte[] address) {
        int firstNonZeroPos = 0;
        while (firstNonZeroPos < address.length && address[firstNonZeroPos] == 0) {
            firstNonZeroPos++;
        }

        byte[] newAddress = new byte[address.length - firstNonZeroPos];
        System.arraycopy(address, firstNonZeroPos, newAddress, 0, address.length - firstNonZeroPos);

        return newAddress;
    }

    public static EthAddress empty() {
        return EthAddress.of(EMPTY_ARRAY);
    }

    public String toString() {
        return Hex.toHexString(address);
    }

    public String normalizedString() {
        return toData().toString();
    }

    public String withLeading0x() {
        return "0x" + this.toString();
    }

    public String normalizedWithLeading0x() {
        return this.toData().withLeading0x();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EthAddress that = (EthAddress) o;
        return Arrays.equals(address, that.address);

    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(address);
    }

    public boolean isEmpty() {
        return Arrays.equals(this.address, EMPTY_ARRAY);
    }

    public EthData toData() {
        return EthData.of(normalize(address));
    }

    private byte[] normalize(byte[] data) {
        byte[] normalizedData = new byte[20];
        System.arraycopy(data, 0, normalizedData, normalizedData.length - data.length, data.length);
        return normalizedData;
    }
}

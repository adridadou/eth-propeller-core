package org.adridadou.ethereum.propeller.values;

import org.apache.commons.lang.ArrayUtils;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Created by davidroon on 19.04.16.
 * This code is released under Apache 2 license
 */
public final class EthData {
    public static final int WORD_SIZE = 32;
    public final byte[] data;

    private EthData(byte[] data) {
        this.data = data;
    }

    public static EthData of(BigInteger b) {
        byte[] bytes = new byte[WORD_SIZE];
        Arrays.fill(bytes, (byte) (b.signum() < 0 ? -1 : 0));
        byte[] biBytes = b.toByteArray();
        int start = biBytes.length == WORD_SIZE + 1 ? 1 : 0;
        int length = Math.min(biBytes.length, WORD_SIZE);
        System.arraycopy(biBytes, start, bytes, WORD_SIZE - length, length);
        return EthData.of(bytes);
    }

    public static EthData of(byte[] data) {
        return new EthData(data);
    }

    public static EthData of(final String data) {
        if (data != null && data.startsWith("0x")) {
            return of(Hex.decode(data.substring(2)));
        }
        return of(Hex.decode(data));
    }

    public static EthData empty() {
        return EthData.of(new byte[0]);
    }

    public static EthData emptyWord() {
        return EthData.of(new byte[WORD_SIZE]);
    }

    public static EthData of(int length) {
        return EthData.of(BigInteger.valueOf(length));
    }

    public static EthData of(long length) {
        return EthData.of(BigInteger.valueOf(length));
    }

    public static EthData of(byte length) {
        return EthData.of(BigInteger.valueOf(length));
    }

    public String withLeading0x() {
        return "0x" + this.toString();
    }

    public String toString() {
        return Hex.toHexString(data);
    }

    public EthData merge(EthData data) {
        if (data.isEmpty()) {
            return this;
        }
        return new EthData(ArrayUtils.addAll(this.data, data.data));
    }

    public boolean isEmpty() {
        return data.length == 0;
    }

    public int length() {
        return data.length;
    }

    public EthData word(int index) {
        byte[] word = ArrayUtils.subarray(data, WORD_SIZE * index, WORD_SIZE * (index + 1));
        if (word.length < WORD_SIZE) {
            word = ArrayUtils.addAll(word, new byte[WORD_SIZE - word.length]);
        }
        return EthData.of(word);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EthData ethData = (EthData) o;

        return Arrays.equals(data, ethData.data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }
}

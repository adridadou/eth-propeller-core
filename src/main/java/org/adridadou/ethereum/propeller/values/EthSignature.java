package org.adridadou.ethereum.propeller.values;

import org.adridadou.ethereum.propeller.exception.EthereumApiException;

import java.math.BigInteger;

import static org.adridadou.ethereum.propeller.values.EthAccount.CURVE_PARAMS;
import static org.adridadou.ethereum.propeller.values.EthAccount.EC_DOMAIN_PARAMETERS;

public class EthSignature {
    private final BigInteger r, s;
    private final byte recId;

    public static EthSignature of(byte[] data) {
        return EthSignature.of(EthData.of(data));
    }

    public static EthSignature of(String data) {
        return EthSignature.of(EthData.of(data));
    }

    public static EthSignature of(EthData data) {
        if (data.isEmpty()) {
            return new EthSignature(BigInteger.ZERO, BigInteger.ZERO, (byte) 0);
        }
        BigInteger r = toUnsigned(data.word(0).data);
        BigInteger s = toUnsigned(data.word(1).data);
        return new EthSignature(r, s, data.data[data.length - 1]);
    }

    private static BigInteger toUnsigned(byte[] data) {
        BigInteger value = new BigInteger(data);
        if (value.signum() == -1) {
            value = new BigInteger(1, data);
        }

        return value;
    }

    public EthSignature(BigInteger r, BigInteger s, byte recId) {
        check(r.signum() >= 0, "r must be positive");
        check(s.signum() >= 0, "s must be positive");
        this.r = r;

        if (s.compareTo(CURVE_PARAMS.getN().shiftRight(1)) > 0) {
            this.s = EC_DOMAIN_PARAMETERS.getN().subtract(s);
        } else {
            this.s = s;
        }

        this.recId = recId;
    }

    private void check(boolean result, String msg) {
        if (!result) {
            throw new EthereumApiException(msg);
        }
    }

    public EthData toData() {
        return EthData.of(this.r)
                .merge(EthData.of(this.s))
                .merge(EthData.of(new byte[]{recId}));
    }

    public BigInteger getS() {
        return s;
    }

    public BigInteger getR() {
        return r;
    }

    public byte getRecId() {
        return recId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EthSignature that = (EthSignature) o;

        if (recId != that.recId) {
            return false;
        }
        if (r != null ? !r.equals(that.r) : that.r != null) {
            return false;
        }
        return s != null ? s.equals(that.s) : that.s == null;
    }

    @Override
    public int hashCode() {
        int result = r != null ? r.hashCode() : 0;
        result = 31 * result + (s != null ? s.hashCode() : 0);
        result = 31 * result + (int) recId;
        return result;
    }

    @Override
    public String toString() {
        return toData().toString();
    }
}

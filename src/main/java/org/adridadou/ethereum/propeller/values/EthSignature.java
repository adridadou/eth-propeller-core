package org.adridadou.ethereum.propeller.values;

import org.adridadou.ethereum.propeller.Crypto;
import org.adridadou.ethereum.propeller.exception.EthereumApiException;
import org.spongycastle.asn1.x9.X9IntegerConverter;
import org.spongycastle.math.ec.ECAlgorithms;
import org.spongycastle.math.ec.ECPoint;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;

import static java.util.Arrays.copyOfRange;
import static org.adridadou.ethereum.propeller.values.EthAccount.CURVE_PARAMS;
import static org.adridadou.ethereum.propeller.values.EthAccount.EC_DOMAIN_PARAMETERS;

public class EthSignature implements Serializable {
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

    public EthAddress ecrecover(EthData data) {
        return recoverPubBytesFromSignature(recId, data.sha3().hash).map(pubKey -> {
            byte[] hash = Crypto.sha3(copyOfRange(pubKey.data, 1, pubKey.length));
            return EthAddress.of(copyOfRange(hash, 12, hash.length));
        }).orElseGet(EthAddress::empty);
    }

    public Optional<EthData> recoverPubBytesFromSignature(byte recId, byte[] messageHash) {
        BigInteger i = BigInteger.valueOf(recId / 2);
        BigInteger x = r.add(i.multiply(EC_DOMAIN_PARAMETERS.getN()));

        if (x.compareTo(EthAccount.CURVE.getQ()) >= 0) {
            return Optional.empty();
        }

        ECPoint R = decompressKey(x, recId);
        if (!R.multiply(EC_DOMAIN_PARAMETERS.getN()).isInfinity()) {
            return Optional.empty();
        }

        BigInteger e = new BigInteger(1, messageHash);

        BigInteger eInv = BigInteger.ZERO.subtract(e).mod(EC_DOMAIN_PARAMETERS.getN());
        BigInteger rInv = r.modInverse(EC_DOMAIN_PARAMETERS.getN());
        BigInteger srInv = rInv.multiply(s).mod(EC_DOMAIN_PARAMETERS.getN());
        BigInteger eInvrInv = rInv.multiply(eInv).mod(EC_DOMAIN_PARAMETERS.getN());
        ECPoint.Fp q = (ECPoint.Fp) ECAlgorithms.sumOfTwoMultiplies(EC_DOMAIN_PARAMETERS.getG(), eInvrInv, R, srInv);
        return Optional.of(EthData.of(q.getEncoded(/* compressed */ false)));
    }

    private ECPoint decompressKey(BigInteger xBN, byte recId) {
        X9IntegerConverter x9 = new X9IntegerConverter();
        byte[] compEnc = x9.integerToBytes(xBN, 1 + x9.getByteLength(EthAccount.CURVE));
        compEnc[0] = (byte) ((recId & 1) == 1 ? 0x03 : 0x02);
        return EthAccount.CURVE.decodePoint(compEnc);
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
        if (!Objects.equals(r, that.r)) {
            return false;
        }
        return Objects.equals(s, that.s);
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

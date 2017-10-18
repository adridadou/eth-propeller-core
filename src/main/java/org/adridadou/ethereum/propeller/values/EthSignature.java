package org.adridadou.ethereum.propeller.values;

import org.adridadou.ethereum.propeller.exception.EthereumApiException;
import org.adridadou.ethereum.propeller.solidity.SolidityType;
import org.adridadou.ethereum.propeller.solidity.converters.encoders.NumberEncoder;

import java.math.BigInteger;

import static org.adridadou.ethereum.propeller.values.EthAccount.CURVE_PARAMS;
import static org.adridadou.ethereum.propeller.values.EthAccount.EC_DOMAIN_PARAMETERS;

public class EthSignature {
    private static final NumberEncoder numberEncoder = new NumberEncoder();
    private final BigInteger r, s;
    private final byte recId;

    public EthSignature(BigInteger r, BigInteger s) {
        this(r, s, (byte) 0);
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
        final byte fixedV = this.recId >= 27
                ? (byte) (this.recId - 27)
                : this.recId;

        return numberEncoder
                .encode(this.r, SolidityType.UINT)
                .merge(numberEncoder.encode(this.s, SolidityType.UINT))
                .merge(EthData.of(new byte[]{fixedV}));
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
}

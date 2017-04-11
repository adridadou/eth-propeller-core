package org.adridadou.ethereum.propeller.values;

import org.adridadou.ethereum.propeller.Crypto;
import org.adridadou.ethereum.propeller.exception.EthereumApiException;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.crypto.ec.CustomNamedCurves;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.jce.spec.ECParameterSpec;
import org.spongycastle.jce.spec.ECPrivateKeySpec;
import org.spongycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;

import static java.util.Arrays.copyOfRange;

/**
 * Created by davidroon on 05.11.16.
 * This code is released under Apache 2 license
 */
public class EthAccount {
    private static final X9ECParameters CURVE_PARAMS = CustomNamedCurves.getByName("secp256k1");
    private static final ECDomainParameters CURVE;
    private static final KeyFactory keyFactory;
    private static final ECParameterSpec CURVE_SPEC;

    static {
        try {
            CURVE = new ECDomainParameters(CURVE_PARAMS.getCurve(), CURVE_PARAMS.getG(), CURVE_PARAMS.getN(), CURVE_PARAMS.getH());
            CURVE_SPEC = new ECParameterSpec(CURVE_PARAMS.getCurve(), CURVE_PARAMS.getG(), CURVE_PARAMS.getN(), CURVE_PARAMS.getH());
            keyFactory = KeyFactory.getInstance("EC", new BouncyCastleProvider());
        } catch (NoSuchAlgorithmException e) {
            throw new EthereumApiException("error while initializing cryptographic tools", e);
        }
    }

    private final BigInteger bigIntPrivateKey;
    private final PrivateKey privateKey;
    private final ECPoint publicKey;
    private final EthAddress address;

    public EthAccount(BigInteger privKey) throws InvalidKeySpecException {
        this.bigIntPrivateKey = privKey;
        this.privateKey = keyFactory.generatePrivate(new ECPrivateKeySpec(privKey, CURVE_SPEC));
        this.publicKey = CURVE.getG().multiply(privKey);
        this.address = computeAddress(this.publicKey);
    }

    public ECPoint getPublicKey() {
        return publicKey;
    }

    public EthAddress getAddress() {
        return address;
    }

    private EthAddress computeAddress(ECPoint publicKey) {
        byte[] pubBytes = publicKey.getEncoded(false);
        byte[] hash = Crypto.sha3(copyOfRange(pubBytes, 1, pubBytes.length));
        return EthAddress.of(copyOfRange(hash, 12, hash.length));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EthAccount that = (EthAccount) o;

        return privateKey != null ? privateKey.equals(that.privateKey) : that.privateKey == null;
    }

    @Override
    public int hashCode() {
        return privateKey != null ? privateKey.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "address:" + getAddress().withLeading0x();
    }

    public BigInteger getBigIntPrivateKey() {
        return bigIntPrivateKey;
    }
}

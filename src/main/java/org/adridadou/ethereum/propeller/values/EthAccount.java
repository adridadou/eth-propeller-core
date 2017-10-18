package org.adridadou.ethereum.propeller.values;

import org.adridadou.ethereum.propeller.Crypto;
import org.adridadou.ethereum.propeller.exception.EthereumApiException;
import org.spongycastle.asn1.sec.SECNamedCurves;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.asn1.x9.X9IntegerConverter;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.crypto.params.ECPrivateKeyParameters;
import org.spongycastle.crypto.signers.ECDSASigner;
import org.spongycastle.crypto.signers.HMacDSAKCalculator;
import org.spongycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.jce.spec.ECParameterSpec;
import org.spongycastle.jce.spec.ECPrivateKeySpec;
import org.spongycastle.math.ec.ECAlgorithms;
import org.spongycastle.math.ec.ECCurve;
import org.spongycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;
import java.util.stream.IntStream;

import static java.util.Arrays.copyOfRange;

/**
 * Created by davidroon on 05.11.16.
 * This code is released under Apache 2 license
 */
public class EthAccount {
    static final ECDomainParameters EC_DOMAIN_PARAMETERS;
    static final X9ECParameters CURVE_PARAMS = SECNamedCurves.getByName("secp256k1");

    private static final ECCurve.Fp CURVE;
    private static final KeyFactory keyFactory;
    private static final ECParameterSpec CURVE_SPEC;

    static {
        try {
            EC_DOMAIN_PARAMETERS = new ECDomainParameters(CURVE_PARAMS.getCurve(), CURVE_PARAMS.getG(), CURVE_PARAMS.getN(), CURVE_PARAMS.getH());
            CURVE = (ECCurve.Fp) EC_DOMAIN_PARAMETERS.getCurve();
            CURVE_SPEC = new ECParameterSpec(CURVE_PARAMS.getCurve(), CURVE_PARAMS.getG(), CURVE_PARAMS.getN(), CURVE_PARAMS.getH());
            keyFactory = KeyFactory.getInstance("EC", new BouncyCastleProvider());
        } catch (NoSuchAlgorithmException e) {
            throw new EthereumApiException("error while initializing cryptographic tools", e);
        }
    }

    private final BigInteger bigIntPrivateKey;
    private final BCECPrivateKey privateKey;
    private final ECPoint publicKey;
    private final EthAddress address;

    public EthAccount(BigInteger privKey) throws InvalidKeySpecException {
        this.bigIntPrivateKey = privKey;
        this.privateKey = (BCECPrivateKey) keyFactory.generatePrivate(new ECPrivateKeySpec(privKey, CURVE_SPEC));
        this.publicKey = EC_DOMAIN_PARAMETERS.getG().multiply(privKey);
        this.address = computeAddress(this.publicKey);
    }

    public ECPoint getPublicKey() {
        return publicKey;
    }

    public EthAddress getAddress() {
        return address;
    }

    public BigInteger getBigIntPrivateKey() {
        return bigIntPrivateKey;
    }

    public boolean verify(EthSignature signature, EthData data) {
        return getRecId(signature.getR(), signature.getS(), data.sha3()) > -1;
    }

    public EthSignature sign(EthData data) {
        Sha3 hash = data.sha3();
        EthSignature sig = doSign(hash);

        return new EthSignature(sig.getR(), sig.getS(), (byte) (getRecId(sig.getR(), sig.getS(), hash) + 27));
    }

    private byte getRecId(BigInteger r, BigInteger s, Sha3 hash) {
        EthData thisKey = EthData.of(publicKey.getEncoded(/* compressed */ false));

        return (byte) IntStream.range(0, 4).filter(i -> recoverPubBytesFromSignature((byte) i, r, s, hash.hash)
                .map(thisKey::equals).orElse(false)).findFirst().orElse(-1);
    }

    private EthSignature doSign(Sha3 hash) {
        ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
        ECPrivateKeyParameters privKeyParams = new ECPrivateKeyParameters(privateKey.getD(), EC_DOMAIN_PARAMETERS);
        signer.init(true, privKeyParams);
        BigInteger[] components = signer.generateSignature(hash.hash);

        return new EthSignature(components[0], components[1], (byte) 0);
    }

    private Optional<EthData> recoverPubBytesFromSignature(byte recId, BigInteger r, BigInteger s, byte[] messageHash) {
        BigInteger i = BigInteger.valueOf(recId / 2);
        BigInteger x = r.add(i.multiply(EC_DOMAIN_PARAMETERS.getN()));

        if (x.compareTo(CURVE.getQ()) >= 0) {
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
        byte[] compEnc = x9.integerToBytes(xBN, 1 + x9.getByteLength(CURVE));
        compEnc[0] = (byte) ((recId & 1) == 1 ? 0x03 : 0x02);
        return CURVE.decodePoint(compEnc);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

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

    private EthAddress computeAddress(ECPoint publicKey) {
        byte[] pubBytes = publicKey.getEncoded(false);
        byte[] hash = Crypto.sha3(copyOfRange(pubBytes, 1, pubBytes.length));
        return EthAddress.of(copyOfRange(hash, 12, hash.length));
    }
}

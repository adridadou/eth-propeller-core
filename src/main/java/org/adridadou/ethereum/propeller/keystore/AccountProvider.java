package org.adridadou.ethereum.propeller.keystore;

import org.adridadou.ethereum.propeller.EthereumFacade;
import org.adridadou.ethereum.propeller.exception.EthereumApiException;
import org.adridadou.ethereum.propeller.values.EthAccount;
import org.adridadou.ethereum.propeller.values.EthData;
import org.spongycastle.crypto.digests.SHA3Digest;
import org.spongycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;

/**
 * Created by davidroon on 26.12.16.
 * This code is released under Apache 2 license
 */
public final class AccountProvider {
    private static final ECGenParameterSpec SECP256K1_CURVE = new ECGenParameterSpec("secp256k1");
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final int BIT_LENGTH = 256;
    private static final Provider provider;
    static {
        Provider p = Security.getProvider("SC");
        provider = p != null ? p : new BouncyCastleProvider();
        provider.put("MessageDigest.ETH-KECCAK-256", "org.ethereum.crypto.cryptohash.Keccak256");
        provider.put("MessageDigest.ETH-KECCAK-512", "org.ethereum.crypto.cryptohash.Keccak512");
    }

    private AccountProvider() {
    }

    public static EthAccount fromPrivateKey(final EthData privateKey) {
        return fromPrivateKey(privateKey.data);
    }

    public static EthAccount fromPrivateKey(final byte[] privateKey) {
        try {
            return new EthAccount(new BigInteger(1, privateKey));
        } catch (InvalidKeySpecException e) {
            throw new EthereumApiException("error while generating the account", e);
        }
    }

    public static EthAccount fromPrivateKey(final BigInteger privateKey) {
        try {
            return new EthAccount(privateKey);
        } catch (InvalidKeySpecException e) {
            throw new EthereumApiException("error while generating the account", e);
        }
    }

    public static EthAccount fromPrivateKey(final String privateKey) {
        return AccountProvider.fromPrivateKey(Hex.decode(privateKey));
    }

    public static EthAccount fromSeed(final String id) {
        return AccountProvider.fromPrivateKey(doSha3(id.getBytes(EthereumFacade.CHARSET)));
    }

    public static SecureKey fromKeystore(final File file) {
        return new FileSecureKey(file);
    }

    public static EthAccount random() {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("EC", provider);
            gen.initialize(SECP256K1_CURVE, secureRandom);
            KeyPair keyPair = gen.generateKeyPair();
            BCECPrivateKey privKey = (BCECPrivateKey)keyPair.getPrivate();
            return fromPrivateKey(privKey.getD());
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            throw new EthereumApiException("error while generating a new private key", e);
        }
    }

    private static byte[] doSha3(byte[] message) {
        SHA3Digest digest = new SHA3Digest(BIT_LENGTH);
        byte[] hash = new byte[digest.getDigestSize()];

        if (message.length != 0) {
            digest.update(message, 0, message.length);
        }
        digest.doFinal(hash, 0);
        return hash;
    }
}

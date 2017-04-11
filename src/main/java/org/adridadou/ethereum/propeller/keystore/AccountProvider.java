package org.adridadou.ethereum.propeller.keystore;

import org.adridadou.ethereum.propeller.EthereumFacade;
import org.adridadou.ethereum.propeller.exception.EthereumApiException;
import org.adridadou.ethereum.propeller.values.EthAccount;
import org.spongycastle.crypto.digests.SHA3Digest;
import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.math.BigInteger;
import java.security.spec.InvalidKeySpecException;

/**
 * Created by davidroon on 26.12.16.
 * This code is released under Apache 2 license
 */
public class AccountProvider {

    public static final int BIT_LENGTH = 256;

    private AccountProvider() {
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

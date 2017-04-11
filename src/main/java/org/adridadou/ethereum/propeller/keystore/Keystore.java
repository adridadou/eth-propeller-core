package org.adridadou.ethereum.propeller.keystore;

import org.adridadou.ethereum.propeller.Crypto;
import org.adridadou.ethereum.propeller.EthereumFacade;
import org.adridadou.ethereum.propeller.exception.EthereumApiException;
import org.adridadou.ethereum.propeller.values.EthAccount;
import org.codehaus.jackson.annotate.JsonSetter;
import org.codehaus.jackson.map.ObjectMapper;
import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.util.Arrays;

/**
 * Created by davidroon on 20.04.16.
 * This code is released under Apache 2 license
 */
public class Keystore {
    public static final int PART_SIZE = 16;
    public static final int KEY_LENGTH = 256;
    private KeystoreCrypto crypto;
    private String id;
    private Integer version;
    private String address;

    public static EthAccount fromKeystore(final File keystore, final String password) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Keystore ksObj = mapper.readValue(keystore, Keystore.class);
        byte[] cipherKey;
        switch (ksObj.getCrypto().getKdf()) {
            case "pbkdf2":
                cipherKey = checkMacSha3(ksObj, password);
                break;
            case "scrypt":
                cipherKey = checkMacScrypt(ksObj, password);
                break;
            default:
                throw new EthereumApiException("non valid algorithm " + ksObj.getCrypto().getCipher());
        }

        byte[] secret = Crypto.decryptAes(Hex.decode(ksObj.getCrypto().getCipherparams().getIv()), cipherKey, Hex.decode(ksObj.getCrypto().getCiphertext()));

        return AccountProvider.fromPrivateKey(secret);
    }

    private static byte[] checkMacSha3(Keystore keystore, String password) throws Exception {
        byte[] salt = Hex.decode(keystore.getCrypto().getKdfparams().getSalt());
        int iterations = keystore.getCrypto().getKdfparams().getC();
        byte[] part = new byte[PART_SIZE];
        byte[] h = Crypto.hash(password, salt, iterations);
        return check(keystore, part, h);
    }

    private static byte[] checkMacScrypt(Keystore keystore, String password) {
        byte[] part = new byte[PART_SIZE];
        KdfParams params = keystore.getCrypto().getKdfparams();
        byte[] h = Crypto.scrypt(password.getBytes(EthereumFacade.CHARSET), Hex.decode(params.getSalt()), params.getN(), params.getR(), params.getP(), params.getDklen());
        return check(keystore, part, h);
    }

    private static byte[] check(Keystore keystore, byte[] part, byte[] h) {
        byte[] cipherText = Hex.decode(keystore.getCrypto().getCiphertext());
        System.arraycopy(h, PART_SIZE, part, 0, PART_SIZE);

        byte[] actual = Crypto.sha3(concat(part, cipherText));

        if (Arrays.equals(actual, Hex.decode(keystore.getCrypto().getMac()))) {
            System.arraycopy(h, 0, part, 0, PART_SIZE);
            return part;
        }

        throw new EthereumApiException("error while loading the private key forNetwork the keystore. Most probably a wrong passphrase");
    }

    private static byte[] concat(byte[] a, byte[] b) {
        int aLen = a.length;
        int bLen = b.length;
        byte[] c = new byte[aLen + bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public KeystoreCrypto getCrypto() {
        return crypto;
    }

    @JsonSetter("crypto")
    public void setCrypto(KeystoreCrypto crypto) {
        this.crypto = crypto;
    }

    @JsonSetter("Crypto")
    public void setCryptoOld(KeystoreCrypto crypto) {
        this.crypto = crypto;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }


}

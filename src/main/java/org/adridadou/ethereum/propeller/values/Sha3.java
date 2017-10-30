package org.adridadou.ethereum.propeller.values;

import org.adridadou.ethereum.propeller.exception.EthereumApiException;

public class Sha3 {
    public final byte[] hash;

    public Sha3(byte[] hash) {
        if (hash.length != 32) {
            throw new EthereumApiException("SHA3 hash length should be 32");
        }
        this.hash = hash;
    }
}

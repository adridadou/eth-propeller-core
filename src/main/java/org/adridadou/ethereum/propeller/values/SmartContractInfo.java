package org.adridadou.ethereum.propeller.values;

import org.adridadou.ethereum.propeller.service.CryptoProvider;

/**
 * Created by davidroon on 21.09.16.
 * This code is released under Apache 2 license
 */
public class SmartContractInfo {
    private final EthAddress address;
    private final CryptoProvider cryptoProvider;

    public SmartContractInfo(EthAddress address, CryptoProvider cryptoProvider) {
        this.address = address;
        this.cryptoProvider = cryptoProvider;
    }

    public EthAddress getAddress() {
        return address;
    }

    public CryptoProvider getCryptoProvider() {
        return cryptoProvider;
    }
}

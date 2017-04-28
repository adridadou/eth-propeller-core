package org.adridadou.ethereum.propeller.values;

/**
 * Created by davidroon on 21.09.16.
 * This code is released under Apache 2 license
 */
public class SmartContractInfo {
    private final EthAddress address;
    private final EthAccount account;

    public SmartContractInfo(EthAddress address, EthAccount account) {
        this.address = address;
        this.account = account;
    }

    public EthAddress getAddress() {
        return address;
    }

    public EthAccount getAccount() {
        return account;
    }
}

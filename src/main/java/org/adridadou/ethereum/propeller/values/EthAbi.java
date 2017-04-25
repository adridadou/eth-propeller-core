package org.adridadou.ethereum.propeller.values;

/**
 * Created by davidroon on 25.04.17.
 * This code is released under Apache 2 license
 */
public class EthAbi {
    private final String abi;

    private EthAbi(String abi) {
        this.abi = abi;
    }

    public static EthAbi of(String abi) {
        return new EthAbi(abi);
    }

    public String getAbi() {
        return abi;
    }
}

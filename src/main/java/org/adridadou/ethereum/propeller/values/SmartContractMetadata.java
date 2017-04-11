package org.adridadou.ethereum.propeller.values;

/**
 * Created by davidroon on 22.12.16.
 * This code is released under Apache 2 license
 */
public class SmartContractMetadata {
    private final String abi;

    public SmartContractMetadata(final String abi) {
        this.abi = abi;
    }

    public String getAbi() {
        return abi;
    }
}

package org.adridadou.ethereum.propeller.solidity;

/**
 * Created by davidroon on 27.03.17.
 * This code is released under Apache 2 license
 */
public class SolidityVersion {
    private final static String ANCHOR = "Version: ";
    private final String version;

    public SolidityVersion(String version) {
        int begin = version.indexOf(ANCHOR);
        this.version = version.substring(begin + ANCHOR.length());
    }

    public String getVersion() {
        return version;
    }
}

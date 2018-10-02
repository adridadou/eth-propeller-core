package org.adridadou.ethereum.propeller.solidity;

import org.adridadou.ethereum.propeller.solidity.abi.AbiEntry;
import org.adridadou.ethereum.propeller.values.EthData;

import java.util.List;

/**
 * Created by davidroon on 27.03.17.
 * This code is released under Apache 2 license
 */
public interface SolidityContractDetails {
    List<AbiEntry> getAbi();
    EthData getBinary();
    String getMetadata();
}

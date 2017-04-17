package org.adridadou.ethereum.propeller.solidity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.adridadou.ethereum.propeller.solidity.abi.AbiEntry;
import org.adridadou.ethereum.propeller.values.EthData;

import java.util.List;

/**
 * Created by davidroon on 27.03.17.
 * This code is released under Apache 2 license
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SolidityContractDetails {
    private final String abi;
    private final String bin;
    private final String metadata;
    private List<AbiEntry> entries;

    public SolidityContractDetails() {
        this(null, null, null);
    }

    public SolidityContractDetails(String abi, String bin, String metadata) {
        this.abi = abi;
        this.bin = bin;
        this.metadata = metadata;
    }

    public String getAbi() {
        return abi;
    }

    public String getBin() {
        return bin;
    }

    public String getMetadata() {
        return metadata;
    }

    public synchronized List<AbiEntry> parseAbi() {
        if (entries == null) {
            entries = AbiEntry.parse(abi);
        }

        return entries;
    }

    public EthData getBinary() {
        return EthData.of(bin);
    }
}

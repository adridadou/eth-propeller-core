package org.adridadou.ethereum.propeller.solidity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.adridadou.ethereum.propeller.solidity.abi.AbiEntry;
import org.adridadou.ethereum.propeller.values.EthData;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SolcSolidityContractDetails implements SolidityContractDetails {

  private final String abi;
  private final String bin;
  private final String metadata;
  private List<AbiEntry> entries;

  public SolcSolidityContractDetails() {
    this(null, null, null);
  }

  public SolcSolidityContractDetails(String abi, String bin, String metadata) {
    this.abi = abi;
    this.bin = bin;
    this.metadata = metadata;
  }

  public String getBin() {
    return bin;
  }

  public String getMetadata() {
    return metadata;
  }

  public synchronized List<AbiEntry> getAbi() {
    if (entries == null) {
      entries = AbiEntry.parse(abi);
    }

    return entries;
  }

  public EthData getBinary() {
    return EthData.of(bin);
  }

}

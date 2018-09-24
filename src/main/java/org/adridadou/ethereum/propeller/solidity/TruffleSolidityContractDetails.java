package org.adridadou.ethereum.propeller.solidity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.adridadou.ethereum.propeller.solidity.abi.AbiEntry;
import org.adridadou.ethereum.propeller.values.EthData;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TruffleSolidityContractDetails implements SolidityContractDetails {

  private final List<AbiEntry> abi;
  private final String bytecode;
  private final String metadata;

  public TruffleSolidityContractDetails() {
    this(null, null, null);
  }

  public TruffleSolidityContractDetails(List<AbiEntry> abi, String bytecode, String metadata) {
    this.abi = abi;
    this.bytecode = bytecode;
    this.metadata = metadata;
  }

  @Override
  public List<AbiEntry> getAbi() {
    return abi;
  }

  @Override
  public String getMetadata() {
    return metadata;
  }

  @Override
  public EthData getBinary() {
    return EthData.of(bytecode);
  }
}

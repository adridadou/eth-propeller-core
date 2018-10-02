package org.adridadou.ethereum.propeller.solidity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SolcCompilationResult implements CompilationResult{

  @JsonProperty("contracts")
  private final Map<String, SolcSolidityContractDetails> contracts;

  public SolcCompilationResult() {
    this(null);
  }

  public SolcCompilationResult(Map<String, SolcSolidityContractDetails> contracts) {
    this.contracts = contracts;
  }

  public Optional<? extends SolidityContractDetails> findContract(final String name) {
    return contracts == null ? Optional.empty() : contracts.entrySet().stream().filter(entry -> entry.getKey().endsWith(name)).map(Map.Entry::getValue).findFirst();
  }

  public Collection<? extends SolidityContractDetails> getContracts() {
    return contracts == null ? null : contracts.values();
  }

}

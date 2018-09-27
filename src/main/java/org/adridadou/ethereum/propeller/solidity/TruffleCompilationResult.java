package org.adridadou.ethereum.propeller.solidity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class TruffleCompilationResult implements CompilationResult {

  private final TruffleSolidityContractDetails contractDetails;

  public TruffleCompilationResult() {
    this(null);
  }

  public TruffleCompilationResult(TruffleSolidityContractDetails contractDetails) {
    this.contractDetails = contractDetails;
  }

  @Override
  public Optional<? extends SolidityContractDetails> findContract(String name) {
    return Optional.of(contractDetails);
  }

  @Override
  public Collection<? extends SolidityContractDetails> getContracts() {
    List<TruffleSolidityContractDetails> result = new ArrayList<>();
    result.add(contractDetails);
    return result;
  }
}

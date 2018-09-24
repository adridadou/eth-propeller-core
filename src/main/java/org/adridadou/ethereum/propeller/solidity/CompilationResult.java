package org.adridadou.ethereum.propeller.solidity;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.adridadou.ethereum.propeller.exception.EthereumApiException;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

/**
 * Created by davidroon on 27.03.17.
 * This code is released under Apache 2 license
 */
public interface CompilationResult {

    static CompilationResult parse(String json) throws IOException {
        if (json == null || json.isEmpty()) {
            throw new EthereumApiException("empty compilation result!");
        }
        try {
            SolcCompilationResult result = new ObjectMapper().readValue(json, SolcCompilationResult.class);
            if(result.getContracts() != null) {
                return result;
            }
            TruffleSolidityContractDetails details = new ObjectMapper().readValue(json, TruffleSolidityContractDetails.class);
            return new TruffleCompilationResult(details);
        } catch (JsonParseException ex) {
            throw new EthereumApiException(json);
        }


    }

    Optional<? extends SolidityContractDetails> findContract(final String name);
    Collection<? extends SolidityContractDetails> getContracts() ;
}

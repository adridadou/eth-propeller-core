package org.adridadou.ethereum.propeller.solidity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.adridadou.ethereum.propeller.exception.EthereumApiException;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Created by davidroon on 27.03.17.
 * This code is released under Apache 2 license
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompilationResult {

    @JsonProperty("contracts")
    private final Map<String, SolidityContractDetails> contracts;

    public CompilationResult() {
        this(null);
    }

    public CompilationResult(Map<String, SolidityContractDetails> contracts) {
        this.contracts = contracts;
    }

    public static CompilationResult parse(String json) throws IOException {
        if (json == null || json.isEmpty()) {
            throw new EthereumApiException("empty compilation result!");
        }
        try {
            return new ObjectMapper().readValue(json, CompilationResult.class);
        } catch (JsonParseException ex) {
            throw new EthereumApiException(json);
        }


    }

    public Optional<SolidityContractDetails> findContract(final String name) {
        return contracts.entrySet().stream().filter(entry -> entry.getKey().endsWith(name)).map(Map.Entry::getValue).findFirst();
    }

    public Collection<SolidityContractDetails> getContracts() {
        return contracts.values();
    }
}

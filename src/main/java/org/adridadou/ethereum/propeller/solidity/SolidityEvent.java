package org.adridadou.ethereum.propeller.solidity;

import org.adridadou.ethereum.propeller.solidity.abi.AbiEntry;
import org.adridadou.ethereum.propeller.solidity.converters.decoders.SolidityTypeDecoder;
import org.adridadou.ethereum.propeller.values.EventInfo;

import java.util.List;

/**
 * Created by davidroon on 02.04.17.
 * This code is released under Apache 2 license
 */
public class SolidityEvent<T> {
    private final AbiEntry description;
    private final List<List<SolidityTypeDecoder>> decoders;
    private final Class<T> entityClass;

    public SolidityEvent(AbiEntry description, List<List<SolidityTypeDecoder>> decoders, Class<T> entityClass) {
        this.description = description;
        this.decoders = decoders;
        this.entityClass = entityClass;
    }

    public boolean match(EventInfo data) {
        return data.getEventSignature().equals(description.signature()) || data.getEventSignature().equals(description.signatureLong());

    }

    public <T> T parseEvent(EventInfo ethData, Class<T> clsResult) {
        return (T) description.decode(ethData.getEventArguments(), decoders, clsResult);
    }

    public Class<T> getEntityClass() {
        return entityClass;
    }
}

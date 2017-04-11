package org.adridadou.ethereum.propeller.solidity;

import org.adridadou.ethereum.propeller.solidity.abi.AbiEntry;
import org.adridadou.ethereum.propeller.solidity.converters.decoders.SolidityTypeDecoder;
import org.adridadou.ethereum.propeller.values.EventInfo;

import java.util.List;

/**
 * Created by davidroon on 02.04.17.
 * This code is released under Apache 2 license
 */
public class SolidityEvent {
    private final AbiEntry description;
    private final List<List<SolidityTypeDecoder>> decoders;

    public SolidityEvent(AbiEntry description, List<List<SolidityTypeDecoder>> decoders) {
        this.description = description;
        this.decoders = decoders;
    }

    public boolean match(EventInfo data) {
        return data.getEventSignature().equals(description.signature()) || data.getEventSignature().equals(description.signatureLong());

    }

    public Object parseEvent(EventInfo ethData, Class<?> clsResult) {
        return description.decode(ethData.getEventArguments(), decoders, clsResult);
    }
}

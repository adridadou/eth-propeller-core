package org.adridadou.ethereum.propeller.solidity;

import org.adridadou.ethereum.propeller.solidity.abi.AbiEntry;
import org.adridadou.ethereum.propeller.solidity.converters.decoders.SolidityTypeDecoder;
import org.adridadou.ethereum.propeller.values.EventData;

import java.util.List;

public abstract class SolidityEvent<T> {
	private final AbiEntry description;
	private final List<List<SolidityTypeDecoder>> decoders;

	public SolidityEvent(AbiEntry description, List<List<SolidityTypeDecoder>> decoders) {
		this.description = description;
		this.decoders = decoders;
	}

	public AbiEntry getDescription() {
		return description;
	}

	public List<List<SolidityTypeDecoder>> getDecoders() {
		return decoders;
	}

	public abstract T parseEvent(EventData eventData);

	public boolean match(EventData data) {
		return data.getEventSignature().equals(description.signature()) || data.getEventSignature().equals(description.signatureLong());
	}

}

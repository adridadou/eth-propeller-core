package org.adridadou.ethereum.propeller.solidity;

import org.adridadou.ethereum.propeller.solidity.abi.AbiEntry;
import org.adridadou.ethereum.propeller.solidity.converters.decoders.SolidityTypeDecoder;
import org.adridadou.ethereum.propeller.values.EventData;

import java.util.List;

public class RawSolidityEvent extends SolidityEvent<List<?>> {
	private final List<Class<?>> eventParameters;

	public RawSolidityEvent(AbiEntry description, List<List<SolidityTypeDecoder>> decoders, List<Class<?>> eventParameters) {
		super(description, decoders);
		this.eventParameters = eventParameters;
	}

	@Override
	public List<Object> parseEvent(EventData eventData) {
		return getDescription().decodeParameters(eventData, getDecoders(), eventParameters);
	}
}

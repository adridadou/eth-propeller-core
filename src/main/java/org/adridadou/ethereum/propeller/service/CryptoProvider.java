package org.adridadou.ethereum.propeller.service;

import org.adridadou.ethereum.propeller.values.EthAddress;
import org.adridadou.ethereum.propeller.values.EthData;
import org.adridadou.ethereum.propeller.values.EthSignature;

public interface CryptoProvider {
	EthAddress getAddress();

	EthSignature sign(EthData data);
}

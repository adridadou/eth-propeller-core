package org.adridadou.ethereum.propeller.service;

import org.adridadou.ethereum.propeller.values.EthAccount;
import org.adridadou.ethereum.propeller.values.EthAddress;

public class PropellerCryptoProvider implements CryptoProvider {
	private final EthAccount account;

	public PropellerCryptoProvider(EthAccount account) {
		this.account = account;
	}

	public static CryptoProvider from(EthAccount account) {
		return new PropellerCryptoProvider(account);
	}

	@Override
	public EthAddress getAddress() {
		return account.getAddress();
	}
}

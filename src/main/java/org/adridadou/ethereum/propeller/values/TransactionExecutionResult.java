package org.adridadou.ethereum.propeller.values;

public class TransactionExecutionResult {
	public final EthHash transactionHash;
	public final Nonce nonce;

	public TransactionExecutionResult(EthHash transactionHash, Nonce nonce) {
		this.transactionHash = transactionHash;
		this.nonce = nonce;
	}
}

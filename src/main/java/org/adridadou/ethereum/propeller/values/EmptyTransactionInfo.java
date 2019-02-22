package org.adridadou.ethereum.propeller.values;

/**
 * Created by felipe.forbeck on 22.02.19.
 * This code is released under Apache 2 license
 */
public class EmptyTransactionInfo extends TransactionInfo {

    public EmptyTransactionInfo() {
        super(EthHash.empty(), new EmptyTransactionReceipt(), TransactionStatus.Unknown, EthHash.empty());
    }

}

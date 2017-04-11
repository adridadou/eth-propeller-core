package org.adridadou.ethereum.propeller.solidity.converters.encoders;

import org.adridadou.ethereum.propeller.values.EthAccount;
import org.adridadou.ethereum.propeller.values.EthData;

/**
 * Created by davidroon on 05.04.17.
 * This code is released under Apache 2 license
 */
public class AccountEncoder implements SolidityTypeEncoder {
    private final AddressEncoder addressEncoder = new AddressEncoder();

    @Override
    public boolean canConvert(Class<?> type) {
        return EthAccount.class.equals(type);
    }

    @Override
    public EthData encode(Object arg) {
        return addressEncoder.encode(((EthAccount) arg).getAddress());
    }
}

package org.adridadou.ethereum.propeller.solidity.converters.decoders;

import org.adridadou.ethereum.propeller.values.EthAddress;
import org.adridadou.ethereum.propeller.values.EthData;

import java.lang.reflect.Type;

/**
 * Created by davidroon on 04.04.17.
 * This code is released under Apache 2 license
 */
public class AddressDecoder implements SolidityTypeDecoder {

    @Override
    public EthAddress decode(Integer index, EthData data, Type resultType) {
        return EthAddress.of(data.word(index).data);
    }

    @Override
    public boolean canDecode(Class<?> resultCls) {
        return EthAddress.class.equals(resultCls);
    }
}

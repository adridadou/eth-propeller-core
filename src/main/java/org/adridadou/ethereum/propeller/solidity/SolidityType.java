package org.adridadou.ethereum.propeller.solidity;

import java.util.Arrays;
import java.util.Optional;

/**
 * Created by davidroon on 02.04.17.
 * This code is released under Apache 2 license
 */
public enum SolidityType {
    UINT, UINT8, UINT16, UINT32, UINT64, UINT128, UINT256,
    INT, INT8, INT16, INT32, INT64, INT128, INT256,
    BOOL, STRING(true), ARRAY(true), BYTES(true), ADDRESS, BYTES32;

    public final boolean isDynamic;

    SolidityType() {
        this.isDynamic = false;
    }


    SolidityType(boolean isFixed) {
        this.isDynamic = isFixed;

    }

    public static Optional<SolidityType> find(String type) {
        final String typeToSearch = type.contains("[") ? type.substring(0, type.indexOf("[")) : type;
        return Arrays.stream(values())
                .filter(value -> value.name().equalsIgnoreCase(typeToSearch)).findFirst();
    }
}

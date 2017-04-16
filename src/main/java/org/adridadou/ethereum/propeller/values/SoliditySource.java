package org.adridadou.ethereum.propeller.values;

import java.io.File;
/**
 * Created by davidroon on 18.09.16.
 * This code is released under Apache 2 license
 */
public interface SoliditySource<T> {
    static SoliditySourceFile from(final File file) {
        return SoliditySourceFile.from(file);
    }

    T getSource();
}

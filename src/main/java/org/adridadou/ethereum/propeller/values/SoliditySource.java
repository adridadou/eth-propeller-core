package org.adridadou.ethereum.propeller.values;

import java.io.File;
import java.io.InputStream;

/**
 * Created by davidroon on 18.09.16.
 * This code is released under Apache 2 license
 */
public interface SoliditySource<T> {

    static SoliditySourceFile from(final File file) {
        return SoliditySourceFile.from(file);
    }

    static SoliditySourceString from(final String source) {
        return SoliditySourceString.from(source);
    }

    static SoliditySourceString from(final InputStream stream) {
        return SoliditySourceString.from(stream);
    }

    T getSource();
}

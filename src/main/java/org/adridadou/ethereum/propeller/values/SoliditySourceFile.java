package org.adridadou.ethereum.propeller.values;

import java.io.File;

/**
 * Created by davidroon on 21.02.17.
 * This code is released under Apache 2 license
 */
public class SoliditySourceFile implements SoliditySource<File> {

    private final File source;

    public SoliditySourceFile(File source) {
        this.source = source;
    }

    public static SoliditySourceFile from(File file) {
        return new SoliditySourceFile(file);

    }

    @Override
    public File getSource() {
        return source;
    }

    @Override
    public String toString() {
        return "source:" + source;
    }
}

package org.adridadou.ethereum.propeller.solidity.abi;


/**
 * Created by davidroon on 28.03.17.
 * This code is released under Apache 2 license
 */
public class AbiParam {
    private final Boolean indexed;
    private final String name;
    private final String type;

    public AbiParam() {
        this(null, null, null);
    }

    public AbiParam(Boolean indexed, String name, String type) {
        this.indexed = indexed;
        this.name = name;
        this.type = type;
    }

    public boolean isIndexed() {
        return Boolean.TRUE.equals(indexed);
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isArray() {
        return type.contains("[");
    }

    public boolean isDynamic() {
        return type.contains("[]") || type.equals("string") || type.equals("bytes");
    }

    @Override
    public String toString() {
        return "AbiParam{" +
                "indexed=" + indexed +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                '}';
    }

    public Integer getArraySize() {
        if (!isArray()) {
            return 0;
        }
        int startIndex = type.indexOf("[");
        int endIndex = type.indexOf("]");
        return Integer.parseInt(type.substring(startIndex + 1, endIndex));
    }
}

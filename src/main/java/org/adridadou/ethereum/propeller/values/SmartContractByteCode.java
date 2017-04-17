package org.adridadou.ethereum.propeller.values;

import org.adridadou.ethereum.propeller.exception.EthereumApiException;
import org.adridadou.ethereum.propeller.swarm.SwarmHash;
import org.spongycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Created by davidroon on 17.12.16.
 * This code is released under Apache 2 license
 */
public final class SmartContractByteCode {
    private static final int DATA_SIZE = 256;
    private static final int HASH_SIZE = 32;
    private static final int START_OF_LINK_INDEX = 7;
    private final byte[] code;

    private SmartContractByteCode(byte[] code) {
        this.code = code;
    }

    public static SmartContractByteCode of(EthData code) {
        return new SmartContractByteCode(code.data);
    }

    public static SmartContractByteCode of(byte[] code) {
        return new SmartContractByteCode(code);
    }

    public static SmartContractByteCode of(String code) {
        return new SmartContractByteCode(Hex.decode(code));
    }

    public Optional<SwarmMetadaLink> getMetadaLink() {
        if (code.length == 0) {
            return Optional.empty();
        }

        byte length1 = code[code.length - 1];
        byte length2 = code[code.length - 2];
        int length = length1 + length2 * DATA_SIZE;
        if (length < code.length) {
            byte[] link = new byte[length];
            System.arraycopy(code, code.length - length, link, 0, length);
            return Optional.of(toMetaDataLink(link));
        }
        return Optional.empty();
    }

    private SwarmMetadaLink toMetaDataLink(byte[] link) {
        String strLink = new String(link, StandardCharsets.UTF_8);
        if (strLink.startsWith("bzzr0")) {
            return toSwarmMetadataLink(link);
        }
        throw new EthereumApiException("unknown protocol forNetwork " + strLink);
    }

    private SwarmMetadaLink toSwarmMetadataLink(byte[] link) {
        byte[] hash = new byte[HASH_SIZE];
        System.arraycopy(link, START_OF_LINK_INDEX, hash, 0, HASH_SIZE);
        return new SwarmMetadaLink(SwarmHash.of(hash));
    }

    public String toString() {
        return Hex.toHexString(code);
    }

    public boolean isEmpty() {
        return code.length == 0;
    }
}

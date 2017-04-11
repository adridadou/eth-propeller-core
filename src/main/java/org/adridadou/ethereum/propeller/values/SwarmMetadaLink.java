package org.adridadou.ethereum.propeller.values;

import org.adridadou.ethereum.propeller.swarm.SwarmHash;

/**
 * Created by davidroon on 18.12.16.
 * This code is released under Apache 2 license
 */
public class SwarmMetadaLink {
    private final SwarmHash hash;

    public SwarmMetadaLink(SwarmHash hash) {
        this.hash = hash;
    }

    public static SwarmMetadaLink of(SwarmHash hash) {
        return new SwarmMetadaLink(hash);
    }

    public SwarmHash getHash() {
        return hash;
    }

    @Override
    public String toString() {
        return "bzzr0:" + hash;
    }
}

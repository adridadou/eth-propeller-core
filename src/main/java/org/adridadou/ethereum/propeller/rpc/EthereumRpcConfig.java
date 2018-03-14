package org.adridadou.ethereum.propeller.rpc;

import org.adridadou.ethereum.propeller.EthereumConfig;

import java.util.concurrent.TimeUnit;

/**
 * Created by davidroon on 25.04.17.
 * This code is released under Apache 2 license
 */
public class EthereumRpcConfig extends EthereumConfig {
    private final boolean pollBlocks;
    private final long pollingFrequence;

    private EthereumRpcConfig(boolean pollBlocks, long pollingFrequence, String swarmUrl, long blockWait) {
        super(swarmUrl, blockWait);
        this.pollBlocks = pollBlocks;
        this.pollingFrequence = pollingFrequence;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isPollBlocks() {
        return pollBlocks;
    }

    public long getPollingFrequence() {
        return pollingFrequence;
    }

    public static class Builder extends EthereumConfig.Builder {
        private boolean pollBlocks;
        private long pollingFrequence = 100;

        public Builder pollBlocks(boolean value) {
            this.pollBlocks = value;
            return this;
        }

        public Builder pollingFrequence(long frequence) {
            this.pollingFrequence = frequence;
            return this;
        }

        public Builder pollingFrequence(long amount, TimeUnit unit) {
            this.pollingFrequence = unit.toMillis(amount);
            return this;
        }

        public EthereumRpcConfig build() {
            return new EthereumRpcConfig(pollBlocks, pollingFrequence, swarmUrl, blockWaitLimit);
        }
    }
}

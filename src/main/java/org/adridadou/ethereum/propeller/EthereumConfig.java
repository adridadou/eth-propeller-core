package org.adridadou.ethereum.propeller;

/**
 * Created by davidroon on 25.04.17.
 * This code is released under Apache 2 license
 */
public class EthereumConfig {
    private final String swarmUrl;
    private final long blockWaitLimit;

    public EthereumConfig(String swarmUrl, long blockWaitLimit) {
        this.swarmUrl = swarmUrl;
        this.blockWaitLimit = blockWaitLimit;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String swarmUrl() {
        return swarmUrl;
    }

    public long blockWaitLimit() {
        return blockWaitLimit;
    }

    public static class Builder {
        private String swarmUrl = "http://swarm-gateways.net";
        private long blockWaitLimit = 16;


        public Builder swarmUrl(String url) {
            this.swarmUrl = url;
            return this;
        }

        public Builder blockWaitLimit(long limit) {
            this.blockWaitLimit = limit;
            return this;
        }

        public EthereumConfig build() {
            return new EthereumConfig(swarmUrl, blockWaitLimit);
        }
    }
}

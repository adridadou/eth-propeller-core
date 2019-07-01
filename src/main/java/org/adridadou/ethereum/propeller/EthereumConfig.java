package org.adridadou.ethereum.propeller;

import org.adridadou.ethereum.propeller.values.GasPrice;

/**
 * Created by davidroon on 25.04.17.
 * This code is released under Apache 2 license
 */
public class EthereumConfig {
    private final String swarmUrl;
    private final long blockWaitLimit;
    private final GasPrice gasPrice;

    public EthereumConfig(String swarmUrl, long blockWaitLimit, GasPrice gasPrice) {
        this.swarmUrl = swarmUrl;
        this.blockWaitLimit = blockWaitLimit;
        this.gasPrice = gasPrice;
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

    public GasPrice getGasPrice() {
        return gasPrice;
    }

    public static class Builder {
        protected String swarmUrl = "http://swarm-gateways.net";
        protected long blockWaitLimit = 16;
        protected GasPrice gasPrice;


        public Builder swarmUrl(String url) {
            this.swarmUrl = url;
            return this;
        }

        public Builder blockWaitLimit(long limit) {
            this.blockWaitLimit = limit;
            return this;
        }

        public Builder fixedGasPrice(GasPrice gasPrice) {
            this.gasPrice = gasPrice;
            return this;
        }

        public EthereumConfig build() {
            return new EthereumConfig(swarmUrl, blockWaitLimit, gasPrice);
        }

    }
}

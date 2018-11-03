package org.adridadou.ethereum.propeller.rpc;

import org.adridadou.ethereum.propeller.EthereumConfig;

import java.util.concurrent.TimeUnit;

/**
 * Created by davidroon on 25.04.17.
 * This code is released under Apache 2 license
 */
public final class EthereumRpcConfig extends EthereumConfig {
    private final boolean pollBlocks;
    private final long pollingFrequence;
    private final AuthenticationType authType;
    private final String userName;
    private final String password;


    private EthereumRpcConfig(boolean pollBlocks, long pollingFrequence, String swarmUrl, long blockWait, AuthenticationType authType, String userName, String password) {
        super(swarmUrl, blockWait);
        this.pollBlocks = pollBlocks;
        this.pollingFrequence = pollingFrequence;
        this.authType = authType;
        this.userName = userName;
        this.password = password;
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

    public AuthenticationType getAuthType() {
        return authType;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public static class Builder extends EthereumConfig.Builder {
        private boolean pollBlocks;
        private long pollingFrequence = 100;
        private AuthenticationType authType = AuthenticationType.NoAuth;
        private String password;
        private String userName;

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

        public Builder basicAuth(String user, String password) {
            this.authType = AuthenticationType.BasicAuth;
            this.userName = user;
            this.password = password;
            return this;
        }

        public EthereumRpcConfig build() {
            return new EthereumRpcConfig(pollBlocks, pollingFrequence, swarmUrl, blockWaitLimit, authType, userName, password);
        }
    }
}


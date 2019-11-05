package org.adridadou.ethereum.propeller.rpc;

import java.util.concurrent.TimeUnit;

import org.adridadou.ethereum.propeller.EthereumConfig;
import org.adridadou.ethereum.propeller.values.GasPrice;

/**
 * Created by davidroon on 25.04.17.
 * This code is released under Apache 2 license
 */
public final class EthereumRpcConfig extends EthereumConfig {
	private final boolean pollBlocks;
    private final long pollingInterval;
    private final AuthenticationType authType;
    private final String userName;
    private final String password;

    private EthereumRpcConfig(boolean pollBlocks, long pollingInterval, String swarmUrl, long blockWait, GasPrice gasPrice,
            AuthenticationType authType, String userName, String password) {
        super(swarmUrl, blockWait, gasPrice);
		this.pollBlocks = pollBlocks;
        this.pollingInterval = pollingInterval;
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

    public long getPollingInterval() {
        return pollingInterval;
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
    	private long pollingInterval = 10000;
        private AuthenticationType authType = AuthenticationType.NoAuth;
        private String password;
        private String userName;

		public Builder pollBlocks(boolean value) {
			this.pollBlocks = value;
			return this;
		}

        public Builder pollingInterval(long frequence) {
            this.pollingInterval = frequence;
            return this;
        }

        public Builder pollingInterval(long amount, TimeUnit unit) {
            this.pollingInterval = unit.toMillis(amount);
            return this;
        }

        public Builder basicAuth(String user, String password) {
            this.authType = AuthenticationType.BasicAuth;
            this.userName = user;
            this.password = password;
            return this;
        }

        public EthereumRpcConfig build() {
            return new EthereumRpcConfig(pollBlocks, pollingInterval, swarmUrl, blockWaitLimit, gasPrice, authType, userName, password);
        }
    }
}


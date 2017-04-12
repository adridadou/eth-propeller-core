package org.adridadou.ethereum.propeller.backend;


import org.adridadou.ethereum.propeller.values.EthAccount;
import org.adridadou.ethereum.propeller.values.EthValue;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by davidroon on 22.01.17.
 * This code is released under Apache 2 license
 */
public class TestConfig {
    public static final int DEFAULT_GAS_LIMIT = 4_700_000;
    public static final long DEFAULT_GAS_PRICE = 50_000_000_000L;
    private final Date initialTime;
    private final long gasLimit;
    private final long gasPrice;
    private final Map<EthAccount, EthValue> balances;

    public TestConfig(Date initialTime, long gasLimit, long gasPrice, Map<EthAccount, EthValue> balances) {
        this.initialTime = initialTime;
        this.gasLimit = gasLimit;
        this.gasPrice = gasPrice;
        this.balances = balances;
    }

    public static Builder builder() {
        return new Builder();
    }

    public long getGasLimit() {
        return gasLimit;
    }

    public long getGasPrice() {
        return gasPrice;
    }

    public Map<EthAccount, EthValue> getBalances() {
        return balances;
    }

    public Date getInitialTime() {
        return initialTime;
    }

    public static class Builder {
        private final Map<EthAccount, EthValue> balances = new HashMap<>();
        private long gasLimit = DEFAULT_GAS_LIMIT;
        private long gasPrice = DEFAULT_GAS_PRICE;
        private Date initialTime = new Date();

        public Builder gasLimit(long gasLimit) {
            this.gasLimit = gasLimit;
            return this;
        }

        public Builder gasPrice(long gasPrice) {
            this.gasPrice = gasPrice;
            return this;
        }

        public Builder balance(EthAccount account, EthValue value) {
            this.balances.put(account, value);
            return this;
        }

        public Builder initialTime(Date date) {
            this.initialTime = date;
            return this;
        }

        public TestConfig build() {
            return new TestConfig(initialTime, gasLimit, gasPrice, balances);
        }
    }
}

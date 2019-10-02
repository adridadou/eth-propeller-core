package org.adridadou.ethereum.propeller.event;

import org.adridadou.ethereum.propeller.values.TransactionReceipt;

import java.math.BigInteger;
import java.util.List;

public class BlockInfo {
    public final long blockNumber;
    public final BigInteger timestamp;
    public final List<TransactionReceipt> receipts;

    public BlockInfo(long blockNumber, BigInteger timestamp, List<TransactionReceipt> receipts) {
        this.blockNumber = blockNumber;
        this.timestamp = timestamp;
        this.receipts = receipts;
    }


    @Override
    public String toString() {
        return "BlockInfo{" +
                "blockNumber=" + blockNumber +
                ", receipts=" + receipts +
                '}';
    }
}

package org.adridadou.ethereum.propeller.event;

import java.util.List;

public class BlockInfo {
    public final long blockNumber;
    public final List<TransactionReceipt> receipts;

    public BlockInfo(long blockNumber, List<TransactionReceipt> receipts) {
        this.blockNumber = blockNumber;
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

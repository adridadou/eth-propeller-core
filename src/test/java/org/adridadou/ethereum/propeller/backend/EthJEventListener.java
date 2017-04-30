package org.adridadou.ethereum.propeller.backend;

import org.adridadou.ethereum.propeller.event.BlockInfo;
import org.adridadou.ethereum.propeller.event.EthereumEventHandler;
import org.adridadou.ethereum.propeller.event.TransactionInfo;
import org.adridadou.ethereum.propeller.event.TransactionStatus;
import org.adridadou.ethereum.propeller.values.EthAddress;
import org.adridadou.ethereum.propeller.values.EthData;
import org.adridadou.ethereum.propeller.values.EthHash;
import org.adridadou.ethereum.propeller.values.EventInfo;
import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.vm.LogInfo;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */
public class EthJEventListener extends EthereumListenerAdapter {
    private final EthereumEventHandler eventHandler;

    EthJEventListener(EthereumEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    @Override
    public void onBlock(Block block, List<TransactionReceipt> receipts) {
        eventHandler.onBlock(new BlockInfo(block.getNumber(), receipts.stream().map(this::toReceipt).collect(Collectors.toList())));
        receipts.forEach(receipt -> eventHandler.onTransactionExecuted(new TransactionInfo(toReceipt(receipt), TransactionStatus.Executed)));
    }

    @Override
    public void onPendingTransactionUpdate(TransactionReceipt txReceipt, PendingTransactionState state, Block block) {
        switch (state) {
            case DROPPED:
                eventHandler.onTransactionDropped(new TransactionInfo(toReceipt(txReceipt), TransactionStatus.Dropped));
                break;
            default:
                break;
        }

    }

    private List<EventInfo> createEventInfoList(List<LogInfo> logs) {
        return logs.stream().map(log -> {
            EthData eventSignature = EthData.of(log.getTopics().get(0).getData());
            EthData eventArguments = EthData.of(log.getData());
            return new EventInfo(eventSignature, eventArguments);
        }).collect(Collectors.toList());
    }

    @Override
    public void onSyncDone(final SyncState syncState) {
        eventHandler.onReady();
    }

    private org.adridadou.ethereum.propeller.event.TransactionReceipt toReceipt(TransactionReceipt transactionReceipt) {
        Transaction tx = transactionReceipt.getTransaction();
        return new org.adridadou.ethereum.propeller.event.TransactionReceipt(EthHash.of(tx.getHash()), EthAddress.of(tx.getSender()), EthAddress.of(tx.getReceiveAddress()), EthAddress.of(tx.getContractAddress()), transactionReceipt.getError(), EthData.of(transactionReceipt.getExecutionResult()), transactionReceipt.isSuccessful() && transactionReceipt.isValid(), createEventInfoList(transactionReceipt.getLogInfoList()));
    }
}

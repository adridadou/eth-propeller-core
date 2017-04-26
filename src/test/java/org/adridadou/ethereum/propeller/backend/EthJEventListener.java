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
import org.ethereum.core.TransactionExecutionSummary;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.vm.LogInfo;

import java.util.Collections;
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
    }

    @Override
    public void onPendingTransactionUpdate(TransactionReceipt txReceipt, PendingTransactionState state, Block block) {
        TransactionStatus transactionStatus;
        switch (state) {
            case PENDING:
            case NEW_PENDING:
                transactionStatus = TransactionStatus.Pending;
                break;
            case DROPPED:
                transactionStatus = TransactionStatus.Dropped;
                break;
            case INCLUDED:
                transactionStatus = TransactionStatus.Included;
                break;
            default:
                transactionStatus = TransactionStatus.Unknown;
                break;
        }
        eventHandler.onPendingTransactionUpdate(new TransactionInfo(toReceipt(txReceipt), transactionStatus));
    }

    @Override
    public void onTransactionExecuted(TransactionExecutionSummary summary) {
        TransactionInfo mainTransaction = new TransactionInfo(toReceipt(summary), TransactionStatus.Executed);
        List<TransactionInfo> internalTransactions = summary.getInternalTransactions().stream()
                .map(internalTransaction -> new TransactionInfo(toReceipt(internalTransaction), TransactionStatus.Executed)).collect(Collectors.toList());

        eventHandler.onTransactionExecuted(mainTransaction, internalTransactions);
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

    private org.adridadou.ethereum.propeller.event.TransactionReceipt toReceipt(TransactionExecutionSummary summary) {
        Transaction tx = summary.getTransaction();
        return new org.adridadou.ethereum.propeller.event.TransactionReceipt(EthHash.of(summary.getTransactionHash()), EthAddress.of(tx.getSender()), EthAddress.of(tx.getReceiveAddress()), EthAddress.empty(), "", EthData.empty(), true, createEventInfoList(summary.getLogs()));
    }

    private org.adridadou.ethereum.propeller.event.TransactionReceipt toReceipt(Transaction tx) {
        return new org.adridadou.ethereum.propeller.event.TransactionReceipt(EthHash.of(tx.getHash()), EthAddress.of(tx.getSender()), EthAddress.of(tx.getReceiveAddress()), EthAddress.empty(), "", EthData.empty(), true, Collections.emptyList());
    }

    private org.adridadou.ethereum.propeller.event.TransactionReceipt toReceipt(TransactionReceipt transactionReceipt) {
        Transaction tx = transactionReceipt.getTransaction();
        return new org.adridadou.ethereum.propeller.event.TransactionReceipt(EthHash.of(tx.getHash()), EthAddress.of(tx.getSender()), EthAddress.of(tx.getReceiveAddress()), EthAddress.of(tx.getContractAddress()), transactionReceipt.getError(), EthData.of(transactionReceipt.getExecutionResult()), transactionReceipt.isSuccessful() && transactionReceipt.isValid(), createEventInfoList(transactionReceipt.getLogInfoList()));
    }
}

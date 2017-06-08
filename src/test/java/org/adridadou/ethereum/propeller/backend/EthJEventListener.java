package org.adridadou.ethereum.propeller.backend;

import org.adridadou.ethereum.propeller.event.BlockInfo;
import org.adridadou.ethereum.propeller.event.EthereumEventHandler;
import org.adridadou.ethereum.propeller.values.*;
import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.vm.DataWord;
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

    static List<EventInfo> createEventInfoList(List<LogInfo> logs) {
        return logs.stream().map(log -> {
            List<DataWord> topics = log.getTopics();
            EthData eventSignature = EthData.of(topics.get(0).getData());
            EthData eventArguments = EthData.of(log.getData());
            List<EthData> indexedArguments = topics.subList(1, topics.size()).stream()
                    .map(dw -> EthData.of(dw.getData()))
                    .collect(Collectors.toList());

            return new EventInfo(eventSignature, eventArguments, indexedArguments);
        }).collect(Collectors.toList());
    }

    static org.adridadou.ethereum.propeller.values.TransactionReceipt toReceipt(TransactionReceipt transactionReceipt) {
        Transaction tx = transactionReceipt.getTransaction();
        return new org.adridadou.ethereum.propeller.values.TransactionReceipt(EthHash.of(tx.getHash()), EthAddress.of(tx.getSender()), EthAddress.of(tx.getReceiveAddress()), EthAddress.of(tx.getContractAddress()), transactionReceipt.getError(), EthData.of(transactionReceipt.getExecutionResult()), transactionReceipt.isSuccessful() && transactionReceipt.isValid(), createEventInfoList(transactionReceipt.getLogInfoList()));
    }

    @Override
    public void onBlock(Block block, List<TransactionReceipt> receipts) {
        eventHandler.onBlock(new BlockInfo(block.getNumber(), receipts.stream()
                .map(EthJEventListener::toReceipt)
                .collect(Collectors.toList())));

        receipts.forEach(receipt -> eventHandler.onTransactionExecuted(new TransactionInfo(EthHash.of(receipt.getTransaction().getHash()), toReceipt(receipt), TransactionStatus.Executed)));
    }

    @Override
    public void onPendingTransactionUpdate(TransactionReceipt txReceipt, PendingTransactionState state, Block block) {
        switch (state) {
            case DROPPED:
                eventHandler.onTransactionDropped(new TransactionInfo(EthHash.of(txReceipt.getTransaction().getHash()), toReceipt(txReceipt), TransactionStatus.Dropped));
                break;
            default:
                break;
        }
    }

    @Override
    public void onSyncDone(final SyncState syncState) {
        eventHandler.onReady();
    }
}

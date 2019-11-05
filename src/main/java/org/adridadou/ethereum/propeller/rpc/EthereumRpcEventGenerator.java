package org.adridadou.ethereum.propeller.rpc;

import org.adridadou.ethereum.propeller.event.BlockInfo;
import org.adridadou.ethereum.propeller.event.EthereumEventHandler;
import org.adridadou.ethereum.propeller.values.TransactionInfo;
import org.adridadou.ethereum.propeller.values.TransactionStatus;
import org.web3j.protocol.core.methods.response.EthBlock;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by davidroon on 30.01.17.
 * This code is released under Apache 2 license
 */
public class EthereumRpcEventGenerator {
    private final List<EthereumEventHandler> ethereumEventHandlers = new ArrayList<>();
    private final EthereumRpc ethereum;

    public EthereumRpcEventGenerator(Web3JFacade web3JFacade, EthereumRpc ethereum, EthereumRpcConfig config) {
        this.ethereum = ethereum;
        if(config.isPollBlocks()) {
        	web3JFacade.observeBlocksPolling(config.getPollingInterval()).subscribe(this::observeBlocks);
		} else {
			web3JFacade.observeBlocks().subscribe(this::observeBlocks);
		}
    }

    private void observeBlocks(EthBlock ethBlock) {
        ethereumEventHandlers.forEach(EthereumEventHandler::onReady);
        BlockInfo param = ethereum.toBlockInfo(ethBlock);
        ethereumEventHandlers.forEach(handler -> handler.onBlock(param));

        ethereumEventHandlers
                .forEach(handler -> param.receipts
                        .stream().map(tx -> new TransactionInfo(tx.hash, tx, TransactionStatus.Executed, tx.blockHash))
                        .forEach(handler::onTransactionExecuted));
    }

    public void addListener(EthereumEventHandler ethereumEventHandler) {
        this.ethereumEventHandlers.add(ethereumEventHandler);
    }
}

package org.adridadou.ethereum.propeller;

import org.adridadou.ethereum.propeller.event.EthereumEventHandler;
import org.adridadou.ethereum.propeller.rpc.EthereumRpc;
import org.adridadou.ethereum.propeller.rpc.EthereumRpcConfig;
import org.adridadou.ethereum.propeller.rpc.Web3JFacade;
import org.adridadou.ethereum.propeller.values.ChainId;
import org.adridadou.ethereum.propeller.values.config.InfuraKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.websocket.WebSocketService;

import java.net.ConnectException;


/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */
public final class RpcEthereumFacadeProvider {
    private static final Logger logger = LoggerFactory.getLogger(RpcEthereumFacadeProvider.class);

    public static final ChainId MAIN_CHAIN_ID = ChainId.id(0);
    public static final ChainId ROPSTEN_CHAIN_ID = ChainId.id(3);
    public static final ChainId KOVAN_CHAIN_ID = ChainId.id(42);
    public static final ChainId RINKEBY_CHAIN_ID = ChainId.id(4);

    private RpcEthereumFacadeProvider() {
    }

    public static EthereumFacade forRemoteNode(final String url, final ChainId chainId) {
        return forRemoteNode(url, chainId, EthereumRpcConfig.builder().build());
    }

    public static EthereumFacade forRemoteNode(final String url, final ChainId chainId, EthereumRpcConfig config) {
        return forRemoteNode(new HttpService(url), chainId, config);
    }

    private static EthereumFacade forRemoteNode(Web3jService web3jService, final ChainId chainId, EthereumRpcConfig config) {
        Web3j w3j = Web3j.build(web3jService);
        Web3JFacade web3j = new Web3JFacade(w3j);
        EthereumRpc ethRpc = new EthereumRpc(web3j, chainId, config);
        EthereumEventHandler eventHandler = new EthereumEventHandler();
        eventHandler.onReady();
        return CoreEthereumFacadeProvider.create(ethRpc, eventHandler, config);
    }

    public static InfuraBuilder forInfura(final InfuraKey key) {
        return new InfuraBuilder(key, EthereumRpcConfig.builder().pollBlocks(true).build());
    }

    public static InfuraBuilder forInfura(final InfuraKey key, EthereumRpcConfig config) {
        return new InfuraBuilder(key, config);
    }

    public static class InfuraBuilder {
        private final InfuraKey key;
        private EthereumRpcConfig config;

        public InfuraBuilder(InfuraKey key, EthereumRpcConfig config) {
            this.key = key;
            this.config = config;
        }

        public EthereumFacade createMain() {
            return forRemoteNode(createW3JService("mainnet.infura.io"), RpcEthereumFacadeProvider.MAIN_CHAIN_ID, config);
        }

        public EthereumFacade createRopsten() {
            return forRemoteNode(createW3JService("ropsten.infura.io"), RpcEthereumFacadeProvider.ROPSTEN_CHAIN_ID, config);
        }

        public EthereumFacade createKovan() {
            return forRemoteNode(createW3JService("kovan.infura.io"), RpcEthereumFacadeProvider.KOVAN_CHAIN_ID, config);
        }

        public EthereumFacade createRinkeby() {
            return forRemoteNode(createW3JService("rinkeby.infura.io"), RpcEthereumFacadeProvider.RINKEBY_CHAIN_ID, config);
        }

        private Web3jService createW3JService(String url) {
            if (!config.isPollBlocks()) {
                try {
                    WebSocketService webSocketService = new WebSocketService("wss://" + url + "/ws", true);
                    webSocketService.connect();
                    return webSocketService;
                } catch (ConnectException ex) {
                    logger.error("Unable to connect to Infura websocket", ex);
                    config = EthereumRpcConfig.builder().pollBlocks(true).build();
                }
            }
            return new HttpService("https://" + url + "/v3/" + key.key);
        }
    }
}

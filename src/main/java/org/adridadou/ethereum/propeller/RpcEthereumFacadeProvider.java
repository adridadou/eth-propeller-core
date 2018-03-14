package org.adridadou.ethereum.propeller;

import org.adridadou.ethereum.propeller.event.EthereumEventHandler;
import org.adridadou.ethereum.propeller.rpc.EthereumRpc;
import org.adridadou.ethereum.propeller.rpc.EthereumRpcConfig;
import org.adridadou.ethereum.propeller.rpc.Web3JFacade;
import org.adridadou.ethereum.propeller.values.ChainId;
import org.adridadou.ethereum.propeller.values.config.InfuraKey;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;


/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */
public class RpcEthereumFacadeProvider {
    public static final ChainId MAIN_CHAIN_ID = ChainId.id(0);
    public static final ChainId ROPSTEN_CHAIN_ID = ChainId.id(3);
    public static final ChainId ETHER_CAMP_CHAIN_ID = ChainId.id(161);
    public static final ChainId KOVAN_CHAIN_ID = ChainId.id(42);
    public static final ChainId RINKEBY_CHAIN_ID = ChainId.id(4);

    private RpcEthereumFacadeProvider() {
    }

    public static EthereumFacade forRemoteNode(final String url, final ChainId chainId) {
        return forRemoteNode(url, chainId, EthereumRpcConfig.builder().build());
    }

    public static EthereumFacade forRemoteNode(final String url, final ChainId chainId, EthereumRpcConfig config) {
        Web3j w3j = Web3j.build(new HttpService(url));
        Web3JFacade web3j = new Web3JFacade(w3j);
        EthereumRpc ethRpc = new EthereumRpc(web3j, chainId, config);
        EthereumEventHandler eventHandler = new EthereumEventHandler();
        eventHandler.onReady();
        return CoreEthereumFacadeProvider.create(ethRpc, eventHandler, config);
    }

    public static InfuraBuilder forInfura(final InfuraKey key) {
        return new InfuraBuilder(key, EthereumRpcConfig.builder().build());
    }

    public static InfuraBuilder forInfura(final InfuraKey key, EthereumRpcConfig config) {
        return new InfuraBuilder(key, config);
    }

    public static class InfuraBuilder {
        private final InfuraKey key;
        private final EthereumRpcConfig config;

        public InfuraBuilder(InfuraKey key, EthereumRpcConfig config) {
            this.key = key;
            this.config = config;
        }

        public EthereumFacade createMain() {
            return forRemoteNode("https://mainnet.infura.io/" + key.key, RpcEthereumFacadeProvider.MAIN_CHAIN_ID, config);
        }

        public EthereumFacade createRopsten() {
            return forRemoteNode("https://ropsten.infura.io/" + key.key, RpcEthereumFacadeProvider.ROPSTEN_CHAIN_ID, config);
        }

        public EthereumFacade createKovan() {
            return forRemoteNode("https://kovan.infura.io/" + key.key, RpcEthereumFacadeProvider.KOVAN_CHAIN_ID, config);
        }

        public EthereumFacade createRinkeby() {
            return forRemoteNode("https://rinkeby.infura.io/" + key.key, RpcEthereumFacadeProvider.RINKEBY_CHAIN_ID, config);
        }
    }
}

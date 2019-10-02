package org.adridadou.ethereum.propeller;

import org.adridadou.ethereum.propeller.converters.future.FutureConverter;
import org.adridadou.ethereum.propeller.event.BlockInfo;
import org.adridadou.ethereum.propeller.event.EthereumEventHandler;
import org.adridadou.ethereum.propeller.exception.EthereumApiException;
import org.adridadou.ethereum.propeller.service.CryptoProvider;
import org.adridadou.ethereum.propeller.service.PropellerCryptoProvider;
import org.adridadou.ethereum.propeller.solidity.*;
import org.adridadou.ethereum.propeller.solidity.abi.AbiParam;
import org.adridadou.ethereum.propeller.solidity.EvmVersion;
import org.adridadou.ethereum.propeller.solidity.converters.SolidityTypeGroup;
import org.adridadou.ethereum.propeller.solidity.converters.decoders.list.CollectionDecoder;
import org.adridadou.ethereum.propeller.solidity.converters.decoders.SolidityTypeDecoder;
import org.adridadou.ethereum.propeller.solidity.converters.encoders.list.CollectionEncoder;
import org.adridadou.ethereum.propeller.solidity.converters.encoders.SolidityTypeEncoder;
import org.adridadou.ethereum.propeller.swarm.SwarmHash;
import org.adridadou.ethereum.propeller.swarm.SwarmService;
import org.adridadou.ethereum.propeller.values.*;
import io.reactivex.Observable;
import org.web3j.abi.datatypes.Event;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.Log;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static java.lang.reflect.Proxy.newProxyInstance;

/**
 * Created by davidroon on 31.03.16.
 * This code is released under Apache 2 license
 */
public class EthereumFacade {
    public static final Charset CHARSET = StandardCharsets.UTF_8;
    private final EthereumContractInvocationHandler handler;
    private final EthereumProxy ethereumProxy;
    private final SwarmService swarmService;
    private final SolidityCompiler solidityCompiler;

    EthereumFacade(EthereumProxy ethereumProxy, SwarmService swarmService, SolidityCompiler solidityCompiler) {
        this.swarmService = swarmService;
        this.solidityCompiler = solidityCompiler;
        this.handler = new EthereumContractInvocationHandler(ethereumProxy);
        this.ethereumProxy = ethereumProxy;
    }

    /**
     * This method defines a new type as void. This means that for this type, no decoder will be used and null will be returned.
     * This is used usually by wrapper projects to define language specific void types, for example Unit in Scala
     *
     * @param cls the class that needs to be seen as Void
     * @return The EthereumFacade object itself
     */
    public EthereumFacade addVoidType(Class<?> cls) {
        ethereumProxy.addVoidClass(cls);
        return this;
    }

    /**
     * This method adds a converter from CompletableFuture to another future type. This is useful if you wnat to integrate a library with its own
     * Future type or for a wrapper project that wants to convert CompletableFuture to another Future type. for example scala Future
     * @param futureConverter the future converter to add
     * @return The EthereumFacade object itself
     */
    public EthereumFacade addFutureConverter(FutureConverter futureConverter) {
        handler.addFutureConverter(futureConverter);
        return this;
    }

    /**
     * Creates a proxy object representing the interface with the smart contract.
     * This method reads the swarm url from the deployed code and then tries to download the smart contract metadata from Swarm.
     * It then reads the ABI from the smart contract's metadata to create the proxy.
     * @param address the address of the smart contract
     * @param account the account to use to send transactions
     * @param contractInterface The interface representing the smart contract
     * @param <T> the proxy object type
     * @return The contract proxy object
     */
    public <T> T createContractProxy(EthAddress address, EthAccount account, Class<T> contractInterface) {
        return createContractProxy(getDetails(address), address, account, contractInterface);
    }

    public SmartContract createSmartContract(SolidityContractDetails contract, EthAddress address, EthAccount account) {
        return createSmartContract(contract, address, PropellerCryptoProvider.from(account));
    }

	public SmartContract createSmartContract(SolidityContractDetails contract, EthAddress address, CryptoProvider cryptoProvider) {
		return ethereumProxy.getSmartContract(contract, address, cryptoProvider);
	}

    public SmartContract createSmartContract(EthAddress address, EthAccount account) {
        return createSmartContract(getDetails(address), address, account);
    }

    public SmartContract createSmartContract(EthAbi abi, EthAddress address, EthAccount account) {
        return createSmartContract(new SolcSolidityContractDetails(abi.getAbi(), null, null), address, account);
    }

	/**
	 * Creates a proxy object representing the interface with the smart contract.
	 * @param abi The ABI of the smart contract
	 * @param address The address of the smart contract
	 * @param account The account to use to send transactions
	 * @param contractInterface The interface representing the smart contract
	 * @param <T> the proxy object type
	 * @return The contract proxy object
	 */
	public <T> T createContractProxy(EthAbi abi, EthAddress address, EthAccount account, Class<T> contractInterface) {
		return createContractProxy(abi, address, PropellerCryptoProvider.from(account), contractInterface);
	}

    /**
     * Creates a proxy object representing the interface with the smart contract.
     * @param abi The ABI of the smart contract
     * @param address The address of the smart contract
     * @param cryptoProvider The crypto provider to use to send transactions
     * @param contractInterface The interface representing the smart contract
     * @param <T> the proxy object type
     * @return The contract proxy object
     */
    public <T> T createContractProxy(EthAbi abi, EthAddress address, CryptoProvider cryptoProvider, Class<T> contractInterface) {
        T proxy = (T) newProxyInstance(contractInterface.getClassLoader(), new Class[]{contractInterface}, handler);
        handler.register(proxy, contractInterface, new SolcSolidityContractDetails(abi.getAbi(), null, null), address, cryptoProvider);
        return proxy;
    }

    /**
     * Creates a proxy object representing the interface with the smart contract.
     * @param details The compiled smart contract
     * @param address The address of the smart contract
     * @param account The account to use to send transactions
     * @param contractInterface The interface representing the smart contract
     * @param <T> the proxy object type
     * @return The contract proxy object
     */
    public <T> T createContractProxy(SolidityContractDetails details, EthAddress address, EthAccount account, Class<T> contractInterface) {
        return createContractProxy(details, address, PropellerCryptoProvider.from(account), contractInterface);
    }

	/**
	 * Creates a proxy object representing the interface with the smart contract.
	 * @param details The compiled smart contract
	 * @param address The address of the smart contract
	 * @param cryptoProvider The crypto provider to use to send transactions
	 * @param contractInterface The interface representing the smart contract
	 * @param <T> the proxy object type
	 * @return The contract proxy object
	 */
	public <T> T createContractProxy(SolidityContractDetails details, EthAddress address, CryptoProvider cryptoProvider, Class<T> contractInterface) {
		T proxy = (T) newProxyInstance(contractInterface.getClassLoader(), new Class[]{contractInterface}, handler);
		handler.register(proxy, contractInterface, details, address, cryptoProvider);
		return proxy;
	}

    /**
     * Publishes the contract
     * @param contract The compiled contract to publish
     * @param account The account that publishes it
     * @param constructorArgs The constructor arguments
     * @return The future address of the newly created smart contract
     */
    public CompletableFuture<EthAddress> publishContract(SolidityContractDetails contract, EthAccount account, Object... constructorArgs) {
        return publishContract(contract, PropellerCryptoProvider.from(account), constructorArgs);
    }

	/**
	 * Publishes the contract
	 * @param contract The compiled contract to publish
	 * @param cryptoProvider The provider that will sign transactions
	 * @param constructorArgs The constructor arguments
	 * @return The future address of the newly created smart contract
	 */
	public CompletableFuture<EthAddress> publishContract(SolidityContractDetails contract, CryptoProvider cryptoProvider, Object... constructorArgs) {
		return ethereumProxy.publish(contract, cryptoProvider, constructorArgs);
	}

    /**
     * Publishes the contract and sends ether at the same time
     * @param contract The compiled contract to publish
     * @param account The account that publishes it
     * @param value How much ether to send while publishing the smart contract
     * @param constructorArgs The constructor arguments
     * @return The future address of the newly created smart contract
     */
    public CompletableFuture<EthAddress> publishContractWithValue(SolidityContractDetails contract, EthAccount account, EthValue value, Object... constructorArgs) {
        return publishContractWithValue(contract, PropellerCryptoProvider.from(account), value, constructorArgs);
    }

	/**
	 * Publishes the contract and sends ether at the same time
	 * @param contract The compiled contract to publish
	 * @param cryptoProvider The provider that will sign transactions
	 * @param value How much ether to send while publishing the smart contract
	 * @param constructorArgs The constructor arguments
	 * @return The future address of the newly created smart contract
	 */
	public CompletableFuture<EthAddress> publishContractWithValue(SolidityContractDetails contract, CryptoProvider cryptoProvider, EthValue value, Object... constructorArgs) {
		return ethereumProxy.publishWithValue(contract, cryptoProvider, value, constructorArgs);
	}

    /**
     * Publishes the smart contract metadata to Swarm
     * @param contract The compiled contract
     * @return The swarm hash
     */
    public SwarmHash publishMetadataToSwarm(SolidityContractDetails contract) {
        return swarmService.publish(contract.getMetadata());
    }

    /**
     * Checks if an address exists
     * @param address The address to check
     * @return Whether it exists or not
     */
    public boolean addressExists(EthAddress address) {
        return ethereumProxy.addressExists(address);
    }

    /**
     * Gets the balance at an address
     * @param addr The address to check
     * @return The current balance
     */
    public EthValue getBalance(EthAddress addr) {
        return ethereumProxy.getBalance(addr);
    }

    /**
     * Gets the balance of an account
     * @param account The account to check
     * @return The current  balance
     */
    public EthValue getBalance(EthAccount account) {
        return ethereumProxy.getBalance(account.getAddress());
    }

    /**
     * Returns the event handler object
     * This object is used to observe transactions and blocks
     * @return The event handler
     */
    public EthereumEventHandler events() {
        return ethereumProxy.events();
    }

    /**
     * Returns the current best block number
     * @return The best block number
     */
    public long getCurrentBlockNumber() {
        return ethereumProxy.getCurrentBlockNumber();
    }

    /**
     * Returns block for provided blocknumber
     *
     * @param blockNumber The block number
     * @return The block if found
     */
    public Optional<BlockInfo> getBlock(long blockNumber) {
        return ethereumProxy.getBlock(blockNumber);
    }

    /*
     * Returns block for provided blockhash
     *
     * @param blockHash The block number
     * @return The block if found
     */
    public Optional<BlockInfo> getBlock(EthHash blockHash) {
        return ethereumProxy.getBlock(blockHash);
    }

    /**
     * Sends ether
     * @param fromAccount The account that sends ether
     * @param to The target address
     * @param value The value to send
     * @return The future details of the call
     */
    public CompletableFuture<CallDetails> sendEther(EthAccount fromAccount, EthAddress to, EthValue value) {
        return sendEther(PropellerCryptoProvider.from(fromAccount), to, value);
    }

	/**
	 * Sends ether
	 * @param fromCryptoProvider The crypto provider that will sign the transaction
	 * @param to The target address
	 * @param value The value to send
	 * @return The future details of the call
	 */
	public CompletableFuture<CallDetails> sendEther(CryptoProvider fromCryptoProvider, EthAddress to, EthValue value) {
		return ethereumProxy.sendTx(value, EthData.empty(), fromCryptoProvider, to);
	}

    /**
     * Sends the transaction
     * @param value The value to send
     * @param data The data to send
     * @param account The account that sends ether
     * @param address The target address
     * @return The future details of the call
     */
    public CompletableFuture<CallDetails> sendTx(EthValue value, EthData data, EthAccount account, EthAddress address) {
        return sendTx(value, data, PropellerCryptoProvider.from(account), address);
    }

	/**
	 * Sends the transaction
	 * @param value The value to send
	 * @param data The data to send
	 * @param cryptoProvider The crypto provider that will sign the transaction
	 * @param address The target address
	 * @return The future details of the call
	 */
	public CompletableFuture<CallDetails> sendTx(EthValue value, EthData data, CryptoProvider cryptoProvider, EthAddress address) {
		return ethereumProxy.sendTx(value, data, cryptoProvider, address);
	}

    /**
     * Returns the current Nonce of an address.
     * It takes into account pending transactions as well
     * @param address The address from which we want the Nonce
     * @return The Nonce
     */
    public Nonce getNonce(EthAddress address) {
        return ethereumProxy.getNonce(address);
    }

    /**
     * Returns the GasUsage of the transaction data.
     * It takes into account additional gas usage for contract creation
     * @param value The value to send
     * @param data The data to send
     * @param account The account used to simulate a transaction
     * @param address The target address
     * @return The GasUsage
     */
    public GasUsage estimateGas(EthValue value, EthData data, EthAccount account, EthAddress address) {
        return estimateGas(value, data, PropellerCryptoProvider.from(account), address);
    }

	/**
	 * Returns the GasUsage of the transaction data.
	 * It takes into account additional gas usage for contract creation
	 * @param value The value to send
	 * @param data The data to send
	 * @param cryptoProvider The crypto provider that signs the transaction
	 * @param address The target address
	 * @return The GasUsage
	 */
	public GasUsage estimateGas(EthValue value, EthData data, CryptoProvider cryptoProvider, EthAddress address) {
		return ethereumProxy.estimateGas(value, data, cryptoProvider, address);
	}

    /**
     * Returns the set of transactions that are being sent by propeller and but not added to the chain yet
     * @param address The transaction sender
     * @return the set of transaction hashes
     */
    public Set<EthHash> getPendingTransactions(EthAddress address) {
        return ethereumProxy.getPendingTransactions(address);
    }

    /**
     * Returns the binary code from a deployed smart contract
     * @param address The smart contract's address
     * @return The code
     */
    public SmartContractByteCode getCode(EthAddress address) {
        return ethereumProxy.getCode(address);
    }

    /**
     * Downloads and returns the smart contract's metadata
     * @param swarmMetadaLink Swarm url
     * @return The metadata
     */
    public SmartContractMetadata getMetadata(SwarmMetadaLink swarmMetadaLink) {
        try {
            return swarmService.getMetadata(swarmMetadaLink.getHash());
        } catch (IOException e) {
            throw new EthereumApiException("error while getting metadata", e);
        }
    }

    /**
     * Compiles the solidity file
     * @param src the source file
	 * @param evmVersion Ethereum virtual machine version (default to latest)
     * @return The compilation result
     */
    public CompilationResult compile(SoliditySourceFile src, Optional<EvmVersion> evmVersion) {
        return solidityCompiler.compileSrc(src, evmVersion);
    }

    public CompilationResult compile(SoliditySourceFile src) {
        return solidityCompiler.compileSrc(src, Optional.empty());
    }

    /**
     * Search an event definition from the ABI
     * @param contract The compiled contract
     * @param eventName The event name
     * @param eventEntity The entity that will represent the event
     * @param <T> The event entity tpye
     * @return The solidity event definition if found
     */
    public <T> Optional<TypedSolidityEvent<T>> findEventDefinition(SolidityContractDetails contract, String eventName, Class<T> eventEntity) {
        return contract.getAbi().stream()
                .filter(entry -> entry.getType().equals("event"))
                .filter(entry -> entry.getName().equals(eventName))
                .filter(entry -> {
                    List<List<SolidityTypeDecoder>> decoders = entry.getInputs().stream().map(ethereumProxy::getDecoders).collect(Collectors.toList());
                    return entry.findConstructor(decoders, eventEntity).isPresent();
                })
                .map(entry -> {
                    List<List<SolidityTypeDecoder>> decoders = entry.getInputs().stream().map(ethereumProxy::getDecoders).collect(Collectors.toList());
                    return new TypedSolidityEvent<>(entry, decoders, eventEntity);
                }).findFirst();
    }

    /**
     * Search an event definition from the ABI
     * @param contract The compiled contract
     * @param eventName The event name
     * @param eventParams the type of each parameter in the event. Useful when you don't want to map it to a class
     * @return The solidity event definition if found
     */
    public Optional<RawSolidityEvent> findEventDefinitionForParameters(SolidityContractDetails contract, String eventName, List<Class<?>> eventParams) {
        return contract.getAbi().stream()
                .filter(entry -> entry.getType().equals("event"))
                .filter(entry -> entry.getName().equals(eventName))
                .filter(entry -> {
                    List<List<SolidityTypeDecoder>> decoders = entry.getInputs().stream().map(ethereumProxy::getDecoders).collect(Collectors.toList());
                    if(decoders.size() != eventParams.size()) {
                        return false;
                    }

                    for(int i = 0; i < decoders.size(); i++) {
                        Class<?> cls = eventParams.get(i);
                        if (decoders.get(i).stream().noneMatch(decoder -> decoder.canDecode(cls))) {
                            return false;
                        }
                    }
                    return true;
                })
                .map(entry -> {
                    List<List<SolidityTypeDecoder>> decoders = entry.getInputs().stream().map(ethereumProxy::getDecoders).collect(Collectors.toList());
                    return new RawSolidityEvent(entry, decoders, eventParams);
                }).findFirst();
    }

    /**
     * Search an event definition from the ABI
     *
     * @param abi         The ABI
     * @param eventName   The event name
     * @param eventParameters The types of each parameter in the event. Useful when you don't want to map the event to a class
     * @return The solidity event definition if found
     */
    public Optional<RawSolidityEvent> findEventDefinitionForParametersByAbi(EthAbi abi, String eventName, List<Class<?>> eventParameters) {
        return findEventDefinitionForParameters(new SolcSolidityContractDetails(abi.getAbi(), "", ""), eventName, eventParameters);
    }

    /**
     * Search an event definition from the ABI
     *
     * @param abi         The ABI
     * @param eventName   The event name
     * @param eventEntity The entity that will represent the event
     * @param <T>         The event entity
     * @return The solidity event definition if found
     */
    public <T> Optional<TypedSolidityEvent<T>> findEventDefinition(EthAbi abi, String eventName, Class<T> eventEntity) {
        return findEventDefinition(new SolcSolidityContractDetails(abi.getAbi(), "", ""), eventName, eventEntity);
    }

    /**
     * Observe an event from a smart contract
     * @param eventDefiniton The event definition
     * @param address The smart contract's address
     * @param <T> The event entity type
     * @return The event observable
     */
    public <T> Observable<T> observeEvents(SolidityEvent<T> eventDefiniton, EthAddress address) {
        return ethereumProxy.observeEvents(eventDefiniton, address);
    }

    /**
     * Observe an event from a smart contract and returns not only the event but the transaction info as well
     *
     * @param eventDefiniton The event definition
     * @param address        The smart contract's address
     * @param <T>            The event entity type
     * @return The event observable with the info
     */
    public <T> Observable<EventInfo<T>> observeEventsWithInfo(SolidityEvent<T> eventDefiniton, EthAddress address) {
        return ethereumProxy.observeEventsWithInfo(eventDefiniton, address);
    }

    /**
     * Returns all the events that happened at a specific block
     *
     * @param blockNumber     The block number
     * @param eventDefinition The event definition
     * @param address         The smart contract's address
     * @param <T> The event entity type
     * @return The list of events
     */
    public <T> List<T> getEventsAtBlock(Long blockNumber, SolidityEvent<T> eventDefinition, EthAddress address) {
        return ethereumProxy.getEventsAtBlock(eventDefinition, address, blockNumber);
    }

    /**
     * Returns all the events that happened at a specific block
     *
     * @param blockHash       The block hash
     * @param eventDefinition The event definition
     * @param address         The smart contract's address
     * @param <T> The event entity type
     * @return The list of events
     */
    public <T> List<T> getEventsAtBlock(EthHash blockHash, SolidityEvent<T> eventDefinition, EthAddress address) {
        return ethereumProxy.getEventsAtBlock(eventDefinition, address, blockHash);
    }

    /**
     * Returns all the events that happened at a specific block
     *
     * @param transactionHash The transactionHash hash
     * @param eventDefinition The event definition
     * @param address         The smart contract's address
     * @param <T>             The event entity type
     * @return The list of events
     */
    public <T> List<T> getEventsAtTransaction(EthHash transactionHash, SolidityEvent<T> eventDefinition, EthAddress address) {
        return ethereumProxy.getEventsAtTransaction(eventDefinition, address, transactionHash);
    }

    /**
     * Returns all the events that happened at a specific block
     *
     * @param blockNumber     The block number
     * @param eventDefinition The event definition
     * @param address         The smart contract's address
     * @param <T>             The event entity type
     * @return The list of events
     */
    public <T> List<EventInfo<T>> getEventsAtBlockWithInfo(Long blockNumber, SolidityEvent<T> eventDefinition, EthAddress address) {
        return ethereumProxy.getEventsAtBlockWithInfo(eventDefinition, address, blockNumber);
    }

    /**
     * Returns all the events that happened at a specific block
     *
     * @param blockHash       The block hash
     * @param eventDefinition The event definition
     * @param address         The smart contract's address
     * @param <T>             The event entity type
     * @return The list of events
     */
    public <T> List<EventInfo<T>> getEventsAtBlockWithInfo(EthHash blockHash, SolidityEvent<T> eventDefinition, EthAddress address) {
        return ethereumProxy.getEventsAtBlockWithInfo(eventDefinition, address, blockHash);
    }

    /**
     * Returns all the events that happened at a specific block
     *
     * @param transactionHash The transactionHash hash
     * @param eventDefinition The event definition
     * @param address         The smart contract's address
     * @param <T>             The event entity type
     * @return The list of events
     */
    public <T> List<EventInfo<T>> getEventsAtTransactionWithInfo(EthHash transactionHash, SolidityEvent<T> eventDefinition, EthAddress address) {
        return ethereumProxy.getEventsAtTransactionWithInfo(eventDefinition, address, transactionHash);
    }


    /**
     * Returns all the events that happened at a smart contract matching an event signature and indexed parameters
     *
     * @param fromBlock From which block to search from for the events
     * @param toBlock Latest block which should be searched from for the events
     * @param eventDefiniton Event definition that should be matched
     * @param address address of the smart contract that emits the events
     * @param optionalTopics Optional indexed event parameters, passed as 64 character hexidecimal string
	 * @return the list of event data (logs) based on the query parameters
     */
    public List<EventData> getLogs(Optional<DefaultBlockParameter> fromBlock, Optional<DefaultBlockParameter> toBlock, SolidityEvent eventDefiniton, EthAddress address, String... optionalTopics) {
        return ethereumProxy.getLogs(fromBlock.orElse(DefaultBlockParameterName.EARLIEST), toBlock.orElse(DefaultBlockParameterName.LATEST), eventDefiniton, address, optionalTopics);
    }

    /**
     * Encodes an argument manually. This can be useful when you need to send a value to a bytes or bytes32 input
     * @param arg The argument to encode
     * @param solidityType Which solidity type is the argument represented
     * @return The Encoded result
     */
    public EthData encode(Object arg, SolidityType solidityType) {
        return Optional.of(arg).map(argument -> {
            SolidityTypeEncoder encoder = ethereumProxy.getEncoders(new AbiParam(false, "", solidityType.name()))
                    .stream().filter(enc -> enc.canConvert(arg.getClass()))
                    .findFirst().orElseThrow(() -> new EthereumApiException("cannot convert the type " + argument.getClass() + " to the solidty type " + solidityType));

            return encoder.encode(arg, solidityType);
        }).orElseGet(EthData::empty);
    }

    public <T> T decode(EthData data, SolidityType solidityType, Class<T> cls) {
        return decode(0, data, solidityType, cls);
    }
    /**
     * Decodes an ouput. This is useful when a function returns bytes or bytes32 and you want to cast it to a specific type
     * @param index It can be that more than one value has been encoded in the data. This is the index of this value. It starts with 0
     * @param data The data to decode
     * @param solidityType The target solidity type
     * @param cls The target class
     * @param <T> The value type
     * @return The decoded value
     */
    public <T> T decode(Integer index, EthData data, SolidityType solidityType, Class<T> cls) {
        if (ethereumProxy.isVoidType(cls)) {
            return null;
        }
        SolidityTypeDecoder decoder = ethereumProxy.getDecoders(new AbiParam(false, "", solidityType.name()))
                .stream()
                .filter(dec -> dec.canDecode(cls))
                .findFirst().orElseThrow(() -> new EthereumApiException("cannot decode " + solidityType.name() + " to " + cls.getTypeName()));

        return (T) decoder.decode(index, data, cls);
    }

    public ChainId getChainId() {
        return ethereumProxy.getChainId();
    }

    /**
     * Gets info for the transaction with the specific hash
     *
     * @param hash The hash of the transaction
     * @return the info
     */
    public Optional<TransactionInfo> getTransactionInfo(EthHash hash) {
        return ethereumProxy.getTransactionInfo(hash);
    }

    public EthereumFacade addDecoder(SolidityTypeGroup solidityTypeGroup, SolidityTypeDecoder decoder) {
        ethereumProxy.addDecoder(solidityTypeGroup, decoder);
        return this;
    }

    public EthereumFacade addEncoder(SolidityTypeGroup solidityTypeGroup, SolidityTypeEncoder encoder) {
        ethereumProxy.addEncoder(solidityTypeGroup, encoder);
        return this;
    }

    public EthereumFacade addListDecoder(final Class<? extends CollectionDecoder> decoder) {
        ethereumProxy.addListDecoder(decoder);
        return this;
    }

    public EthereumFacade addListEncoder(final Class<? extends CollectionEncoder> encoder) {
        ethereumProxy.addListEncoder(encoder);
        return this;
    }

    private SolidityContractDetails getDetails(final EthAddress address) {
        SmartContractByteCode code = ethereumProxy.getCode(address);
        SmartContractMetadata metadata = getMetadata(code.getMetadaLink().orElseThrow(() -> new EthereumApiException("no metadata link found for smart contract on address " + address.toString())));
        return new SolcSolidityContractDetails(metadata.getAbi(), "", "");
    }
}

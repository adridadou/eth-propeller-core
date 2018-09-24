package org.adridadou.ethereum.propeller;

import org.adridadou.ethereum.propeller.converters.future.FutureConverter;
import org.adridadou.ethereum.propeller.event.EthereumEventHandler;
import org.adridadou.ethereum.propeller.exception.EthereumApiException;
import org.adridadou.ethereum.propeller.solidity.*;
import org.adridadou.ethereum.propeller.solidity.abi.AbiParam;
import org.adridadou.ethereum.propeller.solidity.converters.SolidityTypeGroup;
import org.adridadou.ethereum.propeller.solidity.converters.decoders.SolidityTypeDecoder;
import org.adridadou.ethereum.propeller.solidity.converters.encoders.SolidityTypeEncoder;
import org.adridadou.ethereum.propeller.swarm.SwarmHash;
import org.adridadou.ethereum.propeller.swarm.SwarmService;
import org.adridadou.ethereum.propeller.values.*;
import rx.Observable;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
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
        return ethereumProxy.getSmartContract(contract, address, account);
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
        T proxy = (T) newProxyInstance(contractInterface.getClassLoader(), new Class[]{contractInterface}, handler);
        handler.register(proxy, contractInterface, new SolcSolidityContractDetails(abi.getAbi(), null, null), address, account);
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
        T proxy = (T) newProxyInstance(contractInterface.getClassLoader(), new Class[]{contractInterface}, handler);
        handler.register(proxy, contractInterface, details, address, account);
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
        return ethereumProxy.publish(contract, account, constructorArgs);
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
        return ethereumProxy.publishWithValue(contract, account, value, constructorArgs);
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
     * Sends ether
     * @param fromAccount The account that sends ether
     * @param to The target address
     * @param value The value to send
     * @return The future execution result
     */
    public CompletableFuture<EthExecutionResult> sendEther(EthAccount fromAccount, EthAddress to, EthValue value) {
        return ethereumProxy.sendTx(value, EthData.empty(), fromAccount, to)
                .thenCompose(CallDetails::getResult)
                .thenApply(result -> new EthExecutionResult(result.executionResult));
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
     * @return The compilation result
     */
    public CompilationResult compile(SoliditySourceFile src) {
        return solidityCompiler.compileSrc(src);
    }

    /**
     * Search an event definition from the ABI
     * @param contract The compiled contract
     * @param eventName The event name
     * @param eventEntity The entity that will represent the event
     * @param <T> The event entity tpye
     * @return The solidity event definition if found
     */
    public <T> Optional<SolidityEvent<T>> findEventDefinition(SolidityContractDetails contract, String eventName, Class<T> eventEntity) {
        return contract.getAbi().stream()
                .filter(entry -> entry.getType().equals("event"))
                .filter(entry -> entry.getName().equals(eventName))
                .filter(entry -> {
                    List<List<SolidityTypeDecoder>> decoders = entry.getInputs().stream().map(ethereumProxy::getDecoders).collect(Collectors.toList());
                    return entry.findConstructor(decoders, eventEntity).isPresent();
                })
                .map(entry -> {
                    List<List<SolidityTypeDecoder>> decoders = entry.getInputs().stream().map(ethereumProxy::getDecoders).collect(Collectors.toList());
                    return new SolidityEvent<>(entry, decoders, eventEntity);
                })
                .findFirst();
    }

    /**
     * Search an event definition from the ABI
     *
     * @param abi         The ABI
     * @param eventName   The event name
     * @param eventEntity The entity that will represent the event
     *  @param <T>         The event entity
     * @return The solidity event definition if found
     */
    public <T> Optional<SolidityEvent<T>> findEventDefinition(EthAbi abi, String eventName, Class<T> eventEntity) {
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
        return ethereumProxy.getEventsAtBlock(eventDefinition, address, eventDefinition.getEntityClass(), blockNumber);
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
        return ethereumProxy.getEventsAtBlock(eventDefinition, address, eventDefinition.getEntityClass(), blockHash);
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
        return ethereumProxy.getEventsAtTransaction(eventDefinition, address, eventDefinition.getEntityClass(), transactionHash);
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
        return ethereumProxy.getEventsAtBlockWithInfo(eventDefinition, address, eventDefinition.getEntityClass(), blockNumber);
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
        return ethereumProxy.getEventsAtBlockWithInfo(eventDefinition, address, eventDefinition.getEntityClass(), blockHash);
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
        return ethereumProxy.getEventsAtTransactionWithInfo(eventDefinition, address, eventDefinition.getEntityClass(), transactionHash);
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

    private SolidityContractDetails getDetails(final EthAddress address) {
        SmartContractByteCode code = ethereumProxy.getCode(address);
        SmartContractMetadata metadata = getMetadata(code.getMetadaLink().orElseThrow(() -> new EthereumApiException("no metadata link found for smart contract on address " + address.toString())));
        return new SolcSolidityContractDetails(metadata.getAbi(), "", "");
    }
}

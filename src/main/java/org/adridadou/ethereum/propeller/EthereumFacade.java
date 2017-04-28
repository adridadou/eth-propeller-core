package org.adridadou.ethereum.propeller;

import org.adridadou.ethereum.propeller.converters.future.FutureConverter;
import org.adridadou.ethereum.propeller.event.EthereumEventHandler;
import org.adridadou.ethereum.propeller.exception.EthereumApiException;
import org.adridadou.ethereum.propeller.solidity.*;
import org.adridadou.ethereum.propeller.solidity.abi.AbiParam;
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

    public EthereumFacade addVoidType(Class<?> cls) {
        ethereumProxy.addVoidClass(cls);
        return this;
    }

    public EthereumFacade addFutureConverter(FutureConverter futureConverter) {
        handler.addFutureConverter(futureConverter);
        return this;
    }

    public <T> T createContractProxy(EthAddress address, EthAccount account, Class<T> contractInterface) {
        return createContractProxy(getDetails(address), address, account, contractInterface);
    }

    public <T> T createContractProxy(EthAbi abi, EthAddress address, EthAccount account, Class<T> contractInterface) {
        T proxy = (T) newProxyInstance(contractInterface.getClassLoader(), new Class[]{contractInterface}, handler);
        handler.register(proxy, contractInterface, new SolidityContractDetails(abi.getAbi(), null, null), address, account);
        return proxy;
    }

    public <T> T createContractProxy(SolidityContractDetails details, EthAddress address, EthAccount account, Class<T> contractInterface) {
        T proxy = (T) newProxyInstance(contractInterface.getClassLoader(), new Class[]{contractInterface}, handler);
        handler.register(proxy, contractInterface, details, address, account);
        return proxy;
    }

    public CompletableFuture<EthAddress> publishContract(SolidityContractDetails contract, EthAccount account, Object... constructorArgs) {
        return ethereumProxy.publish(contract, account, constructorArgs);
    }

    public SwarmHash publishMetadataToSwarm(SolidityContractDetails contract) {
        return swarmService.publish(contract.getMetadata());
    }

    public boolean addressExists(EthAddress address) {
        return ethereumProxy.addressExists(address);
    }

    public EthValue getBalance(EthAddress addr) {
        return ethereumProxy.getBalance(addr);
    }

    public EthValue getBalance(EthAccount account) {
        return ethereumProxy.getBalance(account.getAddress());
    }

    public EthereumEventHandler events() {
        return ethereumProxy.events();
    }

    public CompletableFuture<EthExecutionResult> sendEther(EthAccount fromAccount, EthAddress to, EthValue value) {
        return ethereumProxy.sendTx(value, EthData.empty(), fromAccount, to);
    }

    public Nonce getNonce(EthAddress address) {
        return ethereumProxy.getNonce(address);
    }

    public SmartContractByteCode getCode(EthAddress address) {
        return ethereumProxy.getCode(address);
    }

    public SmartContractMetadata getMetadata(SwarmMetadaLink swarmMetadaLink) {
        try {
            return swarmService.getMetadata(swarmMetadaLink.getHash());
        } catch (IOException e) {
            throw new EthereumApiException("error while getting metadata", e);
        }
    }

    public CompilationResult compile(SoliditySourceFile src) {
        return solidityCompiler.compileSrc(src);
    }

    public <T> Optional<SolidityEvent<T>> findEventDefinition(SolidityContractDetails contract, String eventName, Class<T> eventEntity) {
        return contract.parseAbi().stream()
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

    public <T> Observable<T> observeEvents(SolidityEvent<T> eventDefiniton, EthAddress address) {
        return ethereumProxy.observeEvents(eventDefiniton, address);
    }

    public <T> List<T> getEventsAt(Long blockNumber, SolidityEvent eventDefinition, EthAddress address, Class<T> cls) {
        return ethereumProxy.getEvents(eventDefinition, address,cls, blockNumber);
    }

    public <T> List<T> getEventsAt(EthHash blockHash, SolidityEvent eventDefinition, EthAddress address, Class<T> cls) {
        return ethereumProxy.getEvents(eventDefinition, address,cls, blockHash);
    }

    public EthData encode(Object arg, SolidityType solidityType) {
        return Optional.of(arg).map(argument -> {
            SolidityTypeEncoder encoder = ethereumProxy.getEncoders(new AbiParam(false, "", solidityType.name()))
                    .stream().filter(enc -> enc.canConvert(arg.getClass()))
                    .findFirst().orElseThrow(() -> new EthereumApiException("cannot convert the type " + argument.getClass() + " to the solidty type " + solidityType));

            return encoder.encode(arg, solidityType);
        }).orElseGet(EthData::empty);
    }

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

    private SolidityContractDetails getDetails(final EthAddress address) {
        SmartContractByteCode code = ethereumProxy.getCode(address);
        SmartContractMetadata metadata = getMetadata(code.getMetadaLink().orElseThrow(() -> new EthereumApiException("no metadata link found for smart contract on address " + address.toString())));
        return new SolidityContractDetails(metadata.getAbi(), "", "");
    }
}

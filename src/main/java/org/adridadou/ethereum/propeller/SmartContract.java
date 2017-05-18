package org.adridadou.ethereum.propeller;

import org.adridadou.ethereum.propeller.exception.EthereumApiException;
import org.adridadou.ethereum.propeller.solidity.SolidityContractDetails;
import org.adridadou.ethereum.propeller.solidity.SolidityFunction;
import org.adridadou.ethereum.propeller.solidity.abi.AbiEntry;
import org.adridadou.ethereum.propeller.solidity.converters.decoders.SolidityTypeDecoder;
import org.adridadou.ethereum.propeller.solidity.converters.encoders.SolidityTypeEncoder;
import org.adridadou.ethereum.propeller.values.EthAccount;
import org.adridadou.ethereum.propeller.values.EthAddress;
import org.adridadou.ethereum.propeller.values.EthData;
import org.adridadou.ethereum.propeller.values.EthValue;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.adridadou.ethereum.propeller.values.EthValue.wei;

/**
 * Created by davidroon on 20.04.16.
 * This code is released under Apache 2 license
 */
public class SmartContract {
    private final EthAddress address;
    private final EthereumBackend ethereum;
    private final SolidityContractDetails contract;
    private final EthereumProxy proxy;
    private final EthAccount account;

    SmartContract(SolidityContractDetails contract, EthAccount account, EthAddress address, EthereumProxy proxy, EthereumBackend ethereum) {
        this.contract = contract;
        this.account = account;
        this.proxy = proxy;
        this.address = address;
        this.ethereum = ethereum;
    }

    public List<SolidityFunction> getFunctions() {
        return contract.parseAbi().stream()
                .filter(entry -> "function".equals(entry.getType()))
                .map(this::buildFunction)
                .collect(Collectors.toList());
    }

    public List<SolidityFunction> getConstructors() {
        return contract.parseAbi().stream()
                .filter(entry -> "constructor".equals(entry.getType()))
                .map(this::buildFunction)
                .collect(Collectors.toList());
    }

    private SolidityFunction buildFunction(AbiEntry entry) {
        return new SolidityFunction(entry, getEncoders(entry), getDecoders(entry));
    }

    private List<List<SolidityTypeDecoder>> getDecoders(AbiEntry entry) {
        return entry.getOutputs().stream()
                .map(proxy::getDecoders)
                .collect(Collectors.toList());
    }

    private List<List<SolidityTypeEncoder>> getEncoders(AbiEntry entry) {
        return entry.getInputs().stream()
                .map(proxy::getEncoders)
                .collect(Collectors.toList());
    }

    Object callConstFunction(Method method, EthValue value, Object... args) {
        return getFunction(method).map(func -> {
            EthData data = func.encode(args);
            if (method.getGenericReturnType() instanceof Class && proxy.isVoidType((Class<?>) method.getGenericReturnType())) {
                return null;
            }
            return func.decode(ethereum.constantCall(account, address, value, data), method.getGenericReturnType());
        }).orElseThrow(() -> new EthereumApiException("could not find the function " + method.getName() + " that maches the arguments"));
    }

    CompletableFuture<?> callFunction(Method method, Object... args) {
        return callFunction(wei(0), method, args);
    }

    public CompletableFuture<?> callFunction(Method method, EthValue value, Object... arguments) {
        return callFunction(value, method, arguments);
    }

    CompletableFuture<?> callFunction(EthValue value, Method method, Object... args) {
        return getFunction(method).map(func -> {
            EthData functionCallBytes = func.encode(args);
            return proxy.sendTx(value, functionCallBytes, account, address)
                    .thenApply(receipt -> {
                        Class<?> returnType = getGenericType(method.getGenericReturnType());
                        if (proxy.isVoidType(returnType)) {
                            return null;
                        }
                        return func.decode(receipt.getResult(), returnType);
                    });
        }).orElseThrow(() -> new EthereumApiException("function " + method.getName() + " cannot be found. available:" + getAvailableFunctions()));
    }

    private String getAvailableFunctions() {
        return getFunctions().stream()
                .map(SolidityFunction::getName)
                .collect(Collectors.toList()).toString();
    }

    public EthAddress getAddress() {
        return address;
    }

    private Optional<SolidityFunction> getFunction(Method method) {
        return getFunctions().stream()
                .filter(function -> method.getName().equals(function.getName()) && function.matchParams(method.getParameterTypes()))
                .findFirst();
    }

    Optional<SolidityFunction> getConstructor(Object[] args) {
        return getConstructors().stream()
                .filter(func -> func.matchParams(args)).findFirst();
    }

    private Class<?> getGenericType(Type genericType) {
        return (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[0];
    }
}

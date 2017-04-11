package org.adridadou.ethereum.propeller;

import org.adridadou.ethereum.propeller.converters.future.CompletableFutureConverter;
import org.adridadou.ethereum.propeller.converters.future.FutureConverter;
import org.adridadou.ethereum.propeller.exception.EthereumApiException;
import org.adridadou.ethereum.propeller.solidity.SolidityContractDetails;
import org.adridadou.ethereum.propeller.solidity.SolidityFunction;
import org.adridadou.ethereum.propeller.values.EthAccount;
import org.adridadou.ethereum.propeller.values.EthAddress;
import org.adridadou.ethereum.propeller.values.SmartContractInfo;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.adridadou.ethereum.propeller.values.EthValue.wei;


/**
 * Created by davidroon on 31.03.16.
 * This code is released under Apache 2 license
 */
public class EthereumContractInvocationHandler implements InvocationHandler {

    private final Map<EthAddress, Map<EthAccount, SmartContract>> contracts = new HashMap<>();
    private final EthereumProxy ethereumProxy;
    private final Map<ProxyWrapper, SmartContractInfo> info = new HashMap<>();
    private final List<FutureConverter> futureConverters = new ArrayList<>();


    EthereumContractInvocationHandler(EthereumProxy ethereumProxy) {
        this.ethereumProxy = ethereumProxy;
        this.futureConverters.add(new CompletableFutureConverter());
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        SmartContractInfo contractInfo = info.get(new ProxyWrapper(proxy));
        SmartContract contract = contracts.get(contractInfo.getAddress()).get(contractInfo.getAccount());
        Object[] arguments = Optional.ofNullable(args).orElse(new Object[0]);

        if (method.getReturnType().equals(Void.TYPE)) {
            try {
                contract.callFunction(method, arguments).get();
            } catch (ExecutionException e) {
                throw e.getCause();
            }

            return Void.TYPE;
        } else {
            return findConverter(method.getReturnType()).map(converter -> {
                if (converter.isFutureType(method.getReturnType())) {
                    return converter.convert(contract.callFunction(method, arguments));
                }
                return converter.getPayable(contract, arguments, method);
            }).orElseGet(() -> contract.callConstFunction(method, wei(0), arguments));
        }
    }

    private Optional<FutureConverter> findConverter(Class type) {
        return futureConverters.stream().filter(converter -> converter.isFutureType(type) || converter.isPayableType(type)).findFirst();
    }

    protected <T> void register(T proxy, Class<T> contractInterface, SolidityContractDetails contract, EthAddress address, EthAccount account) {
        if (address.isEmpty()) {
            throw new EthereumApiException("the contract address cannot be empty");
        }
        SmartContract smartContract = ethereumProxy.getSmartContract(contract, address, account);
        verifyContract(smartContract, contractInterface);

        info.put(new ProxyWrapper(proxy), new SmartContractInfo(address, account));
        Map<EthAccount, SmartContract> proxies = contracts.getOrDefault(address, new HashMap<>());
        proxies.put(account, smartContract);
        contracts.put(address, proxies);
    }

    private void verifyContract(SmartContract smartContract, Class<?> contractInterface) {
        Set<Method> interfaceMethods = new HashSet<>(Arrays.asList(contractInterface.getMethods()));
        Set<SolidityFunction> solidityMethods = new HashSet<>(smartContract.getFunctions());

        List<Method> unmatched = interfaceMethods.stream().filter(method -> solidityMethods.stream()
                .noneMatch(solidityMethod -> solidityMethod.matchParams(method.getParameterTypes())))
                .collect(Collectors.toList());

        String unmatchedSolidityMethods = solidityMethods.stream().filter(solidityMethod -> interfaceMethods.stream()
                .noneMatch(method -> solidityMethod.matchParams(method.getParameterTypes())))
                .map(func -> "- " + func.toString())
                .collect(Collectors.joining("\n"));

        if (!unmatched.isEmpty()) {
            String functions = unmatched.stream()
                    .map(method -> "- " + method.getName() + "(" + Arrays.stream(method.getParameterTypes()).map(Class::getSimpleName).collect(Collectors.joining(", ")) + ")")
                    .collect(Collectors.joining("\n"));

            String message = "*** unmatched *** \nsolidity:\n" + unmatchedSolidityMethods +
                    "\njava:\n" + functions;

            throw new EthereumApiException(message);
        }
    }

    public void addFutureConverter(final FutureConverter futureConverter) {
        futureConverters.add(futureConverter);
    }
}

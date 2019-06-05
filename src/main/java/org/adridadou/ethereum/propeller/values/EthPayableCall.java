package org.adridadou.ethereum.propeller.values;

import org.adridadou.ethereum.propeller.SmartContract;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

/**
 * Created by davidroon on 06.06.17.
 */
public class EthPayableCall<T> {

    private final SmartContract contract;
    private final Method method;
    private final Object[] arguments;

    public EthPayableCall(SmartContract contract, Method method, Object[] arguments) {
        this.contract = contract;
        this.method = method;
        this.arguments = arguments;
    }

    public CompletableFuture<EthCall<T>> with(EthValue value) {
        return contract.callFunctionAndGetDetails(value, method, arguments)
                .thenApply(details -> (EthCall<T>) new EthCall<>(details.getNonce(), details.getGasEstimate(), details.getTxHash(), contract.transformDetailsToResult(details, method)));
    }
}

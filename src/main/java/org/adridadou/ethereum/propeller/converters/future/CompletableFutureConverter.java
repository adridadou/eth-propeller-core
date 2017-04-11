package org.adridadou.ethereum.propeller.converters.future;

import org.adridadou.ethereum.propeller.SmartContract;
import org.adridadou.ethereum.propeller.values.Payable;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

/**
 * Created by davidroon on 26.02.17.
 * This code is released under Apache 2 license
 */
public class CompletableFutureConverter implements FutureConverter {
    @Override
    public CompletableFuture convert(CompletableFuture future) {
        return future;
    }

    @Override
    public boolean isFutureType(Class cls) {
        return CompletableFuture.class.equals(cls);
    }

    @Override
    public boolean isPayableType(Class cls) {
        return Payable.class.equals(cls);
    }

    @Override
    public Payable getPayable(SmartContract smartContract, Object[] arguments, Method method) {
        return new Payable(smartContract, method, arguments);
    }
}

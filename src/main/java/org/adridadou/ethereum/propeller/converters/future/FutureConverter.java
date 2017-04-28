package org.adridadou.ethereum.propeller.converters.future;

import org.adridadou.ethereum.propeller.SmartContract;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

/**
 * Created by davidroon on 26.02.17.
 * This code is released under Apache 2 license
 */
public interface FutureConverter {
    <T> Object convert(CompletableFuture<T> future);

    boolean isFutureType(Class cls);

    boolean isPayableType(Class cls);

    Object getPayable(SmartContract smartContract, Object[] arguments, Method method);
}

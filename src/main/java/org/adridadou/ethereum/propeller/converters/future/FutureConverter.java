package org.adridadou.ethereum.propeller.converters.future;

import org.adridadou.ethereum.propeller.SmartContract;
import org.adridadou.ethereum.propeller.values.CallDetails;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

/**
 * Created by davidroon on 26.02.17.
 * This code is released under Apache 2 license
 */
public interface FutureConverter {
    <T> Object convert(CompletableFuture<T> future);

    Object convertWithDetails(CallDetails details, CompletableFuture<?> futureResult);

    boolean isFutureType(Class cls);

    boolean isFutureTypeWithDetails(Class cls);
    boolean isPayableType(Class cls);

    boolean isPayableTypeWithDetails(Class cls);

    default boolean isAnyFutureDependentType(Class cls) {
        return isFutureType(cls) ||
                isFutureTypeWithDetails(cls) ||
                isPayableType(cls) ||
                isPayableTypeWithDetails(cls);
    }

    Object getPayable(SmartContract smartContract, Object[] arguments, Method method);

    Object getPayableWithDetails(SmartContract smartContract, Object[] arguments, Method method);

}

package org.adridadou.ethereum.propeller.util;

import org.adridadou.ethereum.propeller.exception.EthereumApiException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Created by davidroon on 21.04.17.
 * This code is released under Apache 2 license
 */
public final class CastUtil<T> {
    private final Map<Class<?>, Supplier<T>> classes = new HashMap<>();
    private final Map<String, Supplier<T>> classNames = new HashMap<>();
    private String errorMessage = "casting error ...";

    private CastUtil() {
    }

    public static <T> CastUtil<T> matcher() {
        return new CastUtil<>();
    }


    public CastUtil<T> typeNameEquals(String name, Supplier<T> supplier) {
        classNames.put(name, supplier);
        return this;
    }

    public CastUtil<T> typeEquals(Class<?> cls, Supplier<T> supplier) {
        classes.put(cls, supplier);
        return this;
    }

    public CastUtil<T> orElseThrowWithErrorMessage(String msg) {
        this.errorMessage = msg;
        return this;
    }

    public T matches(Class<? extends T> resultCls) {
        return Optional.ofNullable(classes.get(resultCls))
                .orElseGet(() -> Optional.ofNullable(classNames.get(resultCls.getTypeName()))
                        .orElseThrow(() -> new EthereumApiException(errorMessage))).get();
    }
}

package org.adridadou.ethereum.propeller.converters.input;

import java.util.*;

/**
 * Created by davidroon on 17.11.16.
 * This code is released under Apache 2 license
 */
public class InputTypeHandler {
    private static final List<InputTypeConverter> JAVA_INPUT_CONVERTERS = Arrays.asList(
            new EthAddressConverter(),
            new EthAccountConverter(),
            new EthDataConverter(),
            new EthValueConverter(),
            new EnumConverter(),
            new DateConverter()
    );

    private final List<InputTypeConverter> inputConverters = new ArrayList<>();

    public InputTypeHandler() {
        addConverters(JAVA_INPUT_CONVERTERS);
    }

    public void addConverters(final InputTypeConverter... converters) {
        addConverters(Arrays.asList(converters));
    }

    public void addConverters(final Collection<InputTypeConverter> converters) {
        inputConverters.addAll(converters);
    }


    public Optional<InputTypeConverter> getConverter(final Class<?> cls) {
        return inputConverters.stream().filter(converter -> converter.isOfType(cls)).findFirst();
    }

    public Object convert(final Object arg) {
        return getConverter(arg.getClass()).map(converter -> converter.convert(arg))
                .orElse(arg);
    }
}

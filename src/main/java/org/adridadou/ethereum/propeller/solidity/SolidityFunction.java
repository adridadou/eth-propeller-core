package org.adridadou.ethereum.propeller.solidity;

import org.adridadou.ethereum.propeller.exception.EthereumApiException;
import org.adridadou.ethereum.propeller.solidity.abi.AbiEntry;
import org.adridadou.ethereum.propeller.solidity.abi.AbiParam;
import org.adridadou.ethereum.propeller.solidity.converters.decoders.SolidityTypeDecoder;
import org.adridadou.ethereum.propeller.solidity.converters.encoders.NumberEncoder;
import org.adridadou.ethereum.propeller.solidity.converters.encoders.SolidityTypeEncoder;
import org.adridadou.ethereum.propeller.values.EthData;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.adridadou.ethereum.propeller.values.EthData.WORD_SIZE;

/**
 * Created by davidroon on 30.03.17.
 * This code is released under Apache 2 license
 */
public class SolidityFunction {
    private final NumberEncoder numberEncoder = new NumberEncoder();
    private final AbiEntry description;
    private final List<SolidityType> parameters;
    private final List<List<SolidityTypeEncoder>> encoders;
    private final List<List<SolidityTypeDecoder>> decoders;

    public SolidityFunction(AbiEntry abiEntry, List<SolidityType> parameters, List<List<SolidityTypeEncoder>> encoders, List<List<SolidityTypeDecoder>> decoders) {
        this.description = abiEntry;
        this.encoders = encoders;
        this.decoders = decoders;
        this.parameters = parameters;
    }

    public String getName() {
        return description.getName();
    }

    public AbiEntry getDescription() {
        return description;
    }

    public List<SolidityType> getParameters() {
        return parameters;
    }

    public boolean matchParams(Object[] args) {
        if (args.length != encoders.size()) {
            return false;
        }
        for (int i = 0; i < args.length; i++) {
            final Object arg = args[i];
            if (arg != null && encoders.get(i).stream().noneMatch(encoder -> encoder.canConvert(arg.getClass()))) {
                return false;
            }
        }
        return true;
    }

    public boolean matchParams(Class<?>... types) {
        if (types.length != encoders.size()) {
            return false;
        }
        for (int i = 0; i < types.length; i++) {
            final Class<?> type = types[i];
            if (encoders.get(i).stream().noneMatch(encoder -> encoder.canConvert(type))) {
                return false;
            }
        }
        return true;
    }

    public Optional<String> matchParam(Class<?> classType, int paramIndex) {
        if (encoders.get(paramIndex).stream().noneMatch(encoder -> encoder.canConvert(classType))) {
            return Optional.of("no encoder found for converting type " + classType.getSimpleName() + " to solidity type " + parameters.get(paramIndex).name());
        }
        return Optional.empty();
    }

    public EthData encodeParam(Object arg, int index) {
        if (arg == null) {
            return EthData.empty();
        }

        SolidityTypeEncoder encoder = getEncoder(index, arg);
        AbiParam param = description.getInputs().get(index);
        SolidityType solidityType = SolidityType.find(param.getType()).orElseThrow(() -> new EthereumApiException("unknown solidity type " + description.getType()));

        return encoder.encode(arg, solidityType);

    }

    private SolidityTypeEncoder getEncoder(int index, Object arg) {
        return encoders.get(index).stream()
                .filter(enc -> enc.canConvert(arg.getClass()))
                .findFirst().orElseThrow(() -> new EthereumApiException("encoder could not be found for \"" + arg.getClass().getSimpleName() + "\". Serious bug detected!!"));
    }

    public EthData encode(Object... args) {
        EthData result = description.signature();
        int dynamicIndex = args.length * WORD_SIZE;
        List<EthData> dynamicData = new ArrayList<>();
        if(args.length != encoders.size()) {
            throw new EthereumApiException("wrong number of arguments. you provided " + args.length + " arguments but should be " + encoders.size());
        }
        for (int i = 0; i < args.length; i++) {
            final Object arg = args[i];
            if (arg != null) {
                SolidityTypeEncoder encoder = getEncoder(i, arg);
                AbiParam param = description.getInputs().get(i);
                SolidityType solidityType = SolidityType.find(param.getType()).orElseThrow(() -> new EthereumApiException("unknown solidity type " + description.getType()));
                if (solidityType.isDynamic || param.isDynamic()) {
                    result = result.merge(numberEncoder.encode(dynamicIndex, SolidityType.UINT));
                    EthData dynamicEncode = encoder.encode(arg, solidityType);
                    dynamicIndex += dynamicEncode.length();
                    dynamicData.add(dynamicEncode);
                } else {
                    result = result.merge(encoder.encode(arg, solidityType));
                }
            }
        }

        return dynamicData.stream().reduce(result, EthData::merge);
    }

    public boolean isConstant() {
        return description.isConstant();
    }

    public boolean isPayable() {
        return description.isPayable();
    }

    @Override
    public String toString() {
        return "(" + description.getOutputs().stream()
                .map(AbiParam::getType)
                .collect(Collectors.joining(", ")) + ") " + description.getName() + "(" + description.getInputs().stream()
                .map(AbiParam::getType).collect(Collectors.joining(", ")) + ")";
    }

    public Object decode(EthData ethData, Type genericReturnType) {
        return description.decode(ethData, decoders, genericReturnType);
    }

    public List<List<SolidityTypeEncoder>> getEncoders() {
        return encoders;
    }

    public List<List<SolidityTypeDecoder>> getDecoders() {
        return decoders;
    }
}

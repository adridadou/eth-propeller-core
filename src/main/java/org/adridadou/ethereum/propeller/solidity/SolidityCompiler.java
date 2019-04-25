package org.adridadou.ethereum.propeller.solidity;

import org.adridadou.ethereum.propeller.exception.EthereumApiException;
import org.adridadou.ethereum.propeller.values.SoliditySourceFile;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static org.adridadou.ethereum.propeller.solidity.SolidityCompilerOptions.*;

/**
 * Created by davidroon on 27.03.17.
 * This code is released under Apache 2 license
 */
public class SolidityCompiler {

    private static SolidityCompiler compiler;

    public static SolidityCompiler getInstance() {
        if (compiler == null) {
            compiler = new SolidityCompiler();
        }
        return compiler;
    }

    public CompilationResult compileSrc(SoliditySourceFile source, Optional<EvmVersion> evmVersion) {
        List<String> commandParts = prepareCommandOptions(evmVersion, BIN, ABI, AST, INTERFACE, METADATA);
        commandParts.add(source.getSource().getAbsolutePath());

        try {
            return CompilationResult.parse(runProcess(commandParts));
        } catch (IOException e) {
            throw new EthereumApiException("error while waiting for the process to finish", e);
        }
    }

    public CompilationResult compileSrc(SoliditySourceFile source) {
        return compileSrc(source, Optional.empty());
    }

    public SolidityVersion getVersion() {
        List<String> commandParts = new ArrayList<>(Arrays.asList("solc", "--version"));
        return new SolidityVersion(runProcess(commandParts));
    }

    private String runProcess(final List<String> commandParts) {
        try {
            ProcessResult result = new ProcessExecutor()
                    .command(commandParts)
                    .readOutput(true)
                    .execute();

            if (result.getExitValue() != 0) {
                throw new EthereumApiException(result.outputString());
            }
            return result.outputString();
        } catch (InterruptedException | IOException | TimeoutException e) {
            throw new EthereumApiException("error while running process", e);
        }
    }

    private List<String> prepareCommandOptions(Optional<EvmVersion> evmVersion, SolidityCompilerOptions... options) {
        List<String> commandParts = new ArrayList<>();
        commandParts.add("solc");
        if (evmVersion.isPresent()) {
            commandParts.add("--evm-version=" + evmVersion.get().version);
        }
        commandParts.add("--optimize");
        commandParts.add("--combined-json");
        commandParts.add(Arrays.stream(options)
                .map(SolidityCompilerOptions::getName)
                .collect(Collectors.joining(",")));

        return commandParts;
    }

    private List<String> prepareCommandOptions(SolidityCompilerOptions... options) {
        return prepareCommandOptions(Optional.empty(), options);
    }
}

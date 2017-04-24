package org.adridadou.ethereum.propeller.solidity;

import org.adridadou.ethereum.propeller.exception.EthereumApiException;
import org.adridadou.ethereum.propeller.values.SoliditySourceFile;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static org.adridadou.ethereum.propeller.solidity.SolidityCompilerOptions.*;

/**
 * Created by davidroon on 27.03.17.
 * This code is released under Apache 2 license
 */
public class SolidityCompiler {

    private static SolidityCompiler compiler;
    private File solc;

    private SolidityCompiler() {
        try {
            if (solc == null) {
                File targetFolder = Files.createTempDirectory("solc").toFile();
                InputStream is = getClass().getResourceAsStream("/native/" + getOS() + "/solc/file.list");
                Scanner scanner = new Scanner(is);
                while (scanner.hasNext()) {
                    String s = scanner.next();
                    File targetFile = new File(targetFolder, s);
                    InputStream fis = getClass().getResourceAsStream("/native/" + getOS() + "/solc/" + s);
                    Files.copy(fis, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    if (solc == null) {
                        // first file in the list denotes executable
                        solc = targetFile;
                        if (!solc.setExecutable(true)) {
                            throw new EthereumApiException("failed to set solc as executable");
                        }
                    }
                    targetFile.deleteOnExit();
                }
            }
        } catch (IOException e) {
            throw new EthereumApiException("error while initializing solc");
        }
    }

    public static SolidityCompiler getInstance() {
        if (compiler == null) {
            compiler = new SolidityCompiler();
        }
        return compiler;
    }

    public CompilationResult compileSrc(SoliditySourceFile source) {
        List<String> commandParts = prepareCommandOptions(BIN, ABI, AST, INTERFACE, METADATA);
        commandParts.add(source.getSource().getAbsolutePath());

        try {
            return CompilationResult.parse(runProcess(commandParts));
        } catch (IOException e) {
            throw new EthereumApiException("error while waiting for the process to finish", e);
        }
    }

    public SolidityVersion getVersion() {
        try {
            List<String> commandParts = new ArrayList<>(Arrays.asList(solc.getCanonicalPath(), "--version"));
            return new SolidityVersion(runProcess(commandParts));
        } catch (IOException e) {
            throw new EthereumApiException("error while getting solc version", e);
        }
    }

    private String runProcess(final List<String> commandParts) {
        try {
            ProcessResult result = new ProcessExecutor()
                    .directory(solc.getParentFile())
                    .environment("LD_LIBRARY_PATH", solc.getParentFile().getCanonicalPath())
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

    private List<String> prepareCommandOptions(SolidityCompilerOptions... options) {
        List<String> commandParts = new ArrayList<>();
        try {
            commandParts.add(solc.getCanonicalPath());
        } catch (IOException e) {
            throw new EthereumApiException("error while preparing solc command line", e);
        }

        commandParts.add("--combined-json");
        commandParts.add(Arrays.stream(options)
                .map(SolidityCompilerOptions::getName)
                .collect(Collectors.joining(",")));

        return commandParts;
    }

    private String getOS() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            return "win";
        } else if (osName.contains("linux")) {
            return "linux";
        } else if (osName.contains("mac")) {
            return "mac";
        } else {
            throw new EthereumApiException("Can't find solc compiler: unrecognized OS: " + osName);
        }
    }
}

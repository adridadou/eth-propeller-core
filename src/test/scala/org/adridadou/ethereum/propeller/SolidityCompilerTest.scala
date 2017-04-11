package org.adridadou.ethereum.propeller

import java.io.File

import junit.framework.TestCase.{assertEquals, assertTrue}
import org.adridadou.ethereum.propeller.exception.EthereumApiException
import org.adridadou.ethereum.propeller.solidity.SolidityCompiler
import org.adridadou.ethereum.propeller.values.SoliditySource
import org.junit.Test

/**
  * Created by davidroon on 09.04.17.
  * This code is released under Apache 2 license
  */
class SolidityCompilerTest {
  private val solidityCompiler = new SolidityCompiler

  @Test def testGetVersion(): Unit = {
    val solidityVersion = solidityCompiler.getVersion.getVersion
    assertTrue(solidityVersion.startsWith("0.4.10"))
  }

  @Test def testCompilationOfOneFile(): Unit = {
    val result = solidityCompiler.compileSrc(SoliditySource.from(new File("src/test/resources/contract2.sol")))
    val details = result.findContract("myContract2").orElseThrow(() => new EthereumApiException("myContract2 not found"))
    val entries = details.parseAbi
    assertEquals(6, entries.size)
  }

  @Test def testCompilationOfOneFileWithIncludes(): Unit = {
    val result = solidityCompiler.compileSrc(SoliditySource.from(new File("src/test/resources/c1.sol")))
    val details = result.findContract("c1").orElseThrow(() => new EthereumApiException("c1 not found"))
    val entries = details.parseAbi
    assertEquals(3, entries.size)
  }
}

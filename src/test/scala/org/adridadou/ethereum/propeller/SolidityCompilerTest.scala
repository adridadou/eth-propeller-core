package org.adridadou.ethereum.propeller

import java.io.File

import junit.framework.TestCase.{assertEquals, assertTrue}
import org.adridadou.ethereum.propeller.exception.EthereumApiException
import org.adridadou.ethereum.propeller.solidity.SolidityCompiler
import org.adridadou.ethereum.propeller.values.SoliditySource
import org.scalatest.check.Checkers
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by davidroon on 09.04.17.
  * This code is released under Apache 2 license
  */
class SolidityCompilerTest extends FlatSpec with Matchers with Checkers {
  private val solidityCompiler = SolidityCompiler.getInstance()

  "Solidity compiler" should "get the compiler version" in {
    val solidityVersion = solidityCompiler.getVersion.getVersion
    assertTrue(solidityVersion.startsWith("0.4.16"))
  }

  it should "compile a smart contract from a single file" in {
    val result = solidityCompiler.compileSrc(SoliditySource.from(new File("src/test/resources/contract2.sol")))
    val details = result.findContract("myContract2").orElseThrow(() => new EthereumApiException("myContract2 not found"))
    val entries = details.parseAbi
    assertEquals(6, entries.size)
  }

  it should "compile a smart contract from multiple files with import" in {
    val result = solidityCompiler.compileSrc(SoliditySource.from(new File("src/test/resources/c1.sol")))
    val details = result.findContract("c1").orElseThrow(() => new EthereumApiException("c1 not found"))
    val entries = details.parseAbi
    assertEquals(3, entries.size)
  }
}

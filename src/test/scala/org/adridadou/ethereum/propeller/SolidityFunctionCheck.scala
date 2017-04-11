package org.adridadou.ethereum.propeller

import java.io.File

import org.adridadou.ethereum.propeller.exception.EthereumApiException
import org.adridadou.ethereum.propeller.keystore.AccountProvider
import org.adridadou.ethereum.propeller.solidity.{SolidityContractDetails, SolidityFunction}
import org.adridadou.ethereum.propeller.values.{EthAddress, EthData, SoliditySource}
import org.ethereum.core.CallTransaction
import org.junit.Test
import org.mockito.Mockito
import org.scalacheck.Arbitrary._
import org.scalacheck.Prop._
import org.scalatest.Matchers
import org.scalatest.check.Checkers

import scala.collection.JavaConverters._

/**
  * Created by davidroon on 26.03.17.
  * This code is released under Apache 2 license
  */
class SolidityFunctionCheck extends Checkers with Matchers {
  val contractSource: File = new File("src/test/resources/conversionContract.sol")

  @Test
  def checkGenerateSamePublicKeyAndAddress(): Unit = {
    val smartContract: SmartContract = getSmartContract
    val ethjSmartContract: CallTransaction.Contract = getEthjSmartContract

    check(forAll(arbitrary[String])(testStr(smartContract, ethjSmartContract, "strFunc", _)))
    check(forAll(arbitrary[Int])(testInt(smartContract, ethjSmartContract, "intFunc", _)))
  }

  private def testInt(smartContract: SmartContract, ethjSmartContract: CallTransaction.Contract, funcName: String, value: Integer): Boolean = {
    val propellerFunc: SolidityFunction = smartContract.getFunctions.asScala.filter((func: SolidityFunction) => func.getName == funcName).head
    val ethjStrFunc: CallTransaction.Function = ethjSmartContract.functions.filter((func: CallTransaction.Function) => func.name == funcName).head

    val ethjResult = EthData.of(ethjStrFunc.encode(value))
    val propellerResult = propellerFunc.encode(value)

    ethjResult shouldEqual propellerResult

    true
  }

  private def testStr(smartContract: SmartContract, ethjSmartContract: CallTransaction.Contract, funcName: String, value: String): Boolean = {
    val propellerFunc: SolidityFunction = smartContract.getFunctions.asScala.filter((func: SolidityFunction) => func.getName == funcName).head
    val ethjStrFunc: CallTransaction.Function = ethjSmartContract.functions.filter((func: CallTransaction.Function) => func.name == funcName).head

    val ethjResult = EthData.of(ethjStrFunc.encode(value))
    val propellerResult = propellerFunc.encode(value)

    ethjResult shouldEqual propellerResult

    true
  }

  private def getEthjSmartContract: CallTransaction.Contract = {
    val ethjCompiler = org.ethereum.solidity.compiler.SolidityCompiler.getInstance
    val ethjResult = ethjCompiler.compileSrc(contractSource, true, true, org.ethereum.solidity.compiler.SolidityCompiler.Options.ABI)
    val compilationResult = org.ethereum.solidity.compiler.CompilationResult.parse(ethjResult.output)
    new CallTransaction.Contract(compilationResult.contracts.entrySet().iterator().next().getValue.abi)
  }

  private def getSmartContract: SmartContract = {
    val backend: EthereumBackend = Mockito.mock(classOf[EthereumBackend])
    val facade: EthereumFacade = CoreEthereumFacadeProvider.create(backend)
    val proxy: EthereumProxy = facade.getProxy
    val contract: SolidityContractDetails = facade.compile(SoliditySource.from(contractSource)).get.findContract("myContract").orElseThrow(() => new EthereumApiException("myContract not found"))
    proxy.getSmartContract(contract, EthAddress.empty, AccountProvider.fromSeed("test"))
  }
}

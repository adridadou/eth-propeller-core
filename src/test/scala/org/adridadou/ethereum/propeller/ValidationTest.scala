package org.adridadou.ethereum.propeller

import java.io.File
import java.util.concurrent.CompletableFuture

import org.adridadou.ethereum.propeller.backend.TestConfig
import org.adridadou.ethereum.propeller.converters.e2e.SolidityConversionHelper
import org.adridadou.ethereum.propeller.keystore.AccountProvider
import org.adridadou.ethereum.propeller.values.EthValue.ether
import org.adridadou.ethereum.propeller.values.{EthAddress, SoliditySource}
import org.scalatest.check.Checkers
import org.scalatest.{FlatSpec, Matchers}

import scala.util.{Failure, Success, Try}

/**
  * Created by davidroon on 11.04.17.
  * This code is released under Apache 2 license
  */
class ContractValidationTest extends FlatSpec with Matchers with Checkers {

  private val mainAccount = SolidityConversionHelper.mainAccount
  private val ethereum = SolidityConversionHelper.facade
  private val contractSource =
    SoliditySource.from(new File("src/test/resources/validationContract.sol"))

  "Validation" should "throw an exception if an interface method doesn't match any of the solidity functions" in {
    val compiledContract =
      ethereum.compile(contractSource).findContract("validationContract").get
    val errorMessage =
      "*** unmatched *** \nsolidity:\n- (uint256) validation(uint256)\njava:\n- validation(String)"

    Try(
      ethereum.createContractProxy(
        compiledContract,
        EthAddress.of("0x38848348887728239023"),
        mainAccount,
        classOf[ValidationContractMethodMismatch]
      )
    ) match {
      case Success(_)  => fail("the call should have failed")
      case Failure(ex) => ex.getMessage shouldEqual errorMessage
    }
  }

  it should "throw an exception if the return value does not match" in {
    val compiledContract =
      ethereum.compile(contractSource).findContract("validationContract").get
    Try(
      ethereum.createContractProxy(
        compiledContract,
        EthAddress.of("0x38848348887728239023"),
        mainAccount,
        classOf[ValidationContractReturnValueMismatch]
      )
    ) match {
      case Success(_) => fail("the call should have failed")
      case Failure(ex) =>
        ex.getMessage shouldEqual "could not find decoder for (uint256) to java.lang.String"
    }
  }

  it should "ignore solidity return type if the java return type is Void" in {
    val compiledContract =
      ethereum.compile(contractSource).findContract("validationContract").get
    Try(
      ethereum.createContractProxy(
        compiledContract,
        EthAddress.of("0x38848348887728239023"),
        mainAccount,
        classOf[ValidationContract]
      )
    ) match {
      case Success(_)  => succeed
      case Failure(ex) => throw ex
    }
  }

}

trait ValidationContractMethodMismatch {
  def validation(value: String): CompletableFuture[Void]
}

trait ValidationContract {
  def validation(value: Integer): CompletableFuture[Void]
}

trait ValidationContractReturnValueMismatch {
  def validation(value: java.lang.Integer): CompletableFuture[String]
}

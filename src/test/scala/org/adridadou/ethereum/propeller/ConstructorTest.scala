package org.adridadou.ethereum.propeller

import java.io.File

import org.adridadou.ethereum.propeller.backend.{EthereumTest, TestConfig}
import org.adridadou.ethereum.propeller.exception.EthereumApiException
import org.adridadou.ethereum.propeller.keystore.AccountProvider
import org.adridadou.ethereum.propeller.values.EthValue.ether
import org.adridadou.ethereum.propeller.values.SoliditySource
import org.scalatest.check.Checkers
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by davidroon on 11.04.17.
  * This code is released under Apache 2 license
  */
class ConstructorTest extends FlatSpec with Matchers with Checkers {

  private val mainAccount = AccountProvider.fromSeed("hello")
  private val contractSource = SoliditySource.from(new File("src/test/resources/contractConstructor.sol"))
  private val contractDefaultSource = SoliditySource.from(new File("src/test/resources/contractDefaultConstructor.sol"))

  "Constructor" should "use the default constructor if no arguments are passed" in {
    val ethereum = CoreEthereumFacadeProvider.create(new EthereumTest(TestConfig.builder.balance(mainAccount, ether(1000)).build), EthereumConfig.builder().build())
    val compiledContract = ethereum.compile(contractDefaultSource).findContract("ContractDefaultConstructor").get
    val address = ethereum.publishContract(compiledContract, mainAccount).get()
    val myContract = ethereum.createContractProxy(compiledContract, address, mainAccount, classOf[ContractConstructor])
    myContract.value() shouldEqual "hello world"
  }

  "Constructor" should "use the parameter if given" in {
    val ethereum = CoreEthereumFacadeProvider.create(new EthereumTest(TestConfig.builder.balance(mainAccount, ether(1000)).build), EthereumConfig.builder().build())
    val compiledContract = ethereum.compile(contractSource).findContract("ContractConstructor").get
    val address = ethereum.publishContract(compiledContract, mainAccount, "this is a test").get()
    val myContract = ethereum.createContractProxy(compiledContract, address, mainAccount, classOf[ContractConstructor])
    myContract.value() shouldEqual "this is a test"
  }

  "Constructor" should "show an error message if the constructor signature did not match the arguments" in {
    val ethereum = CoreEthereumFacadeProvider.create(new EthereumTest(TestConfig.builder.balance(mainAccount, ether(1000)).build), EthereumConfig.builder().build())
    val compiledContract = ethereum.compile(contractSource).findContract("ContractConstructor").get
    try {
      ethereum.publishContract(compiledContract, mainAccount, 23938.asInstanceOf[java.lang.Integer]).get()
      fail()
    } catch {
      case ex: EthereumApiException => ex.getMessage shouldEqual "No constructor found with params (Integer)"
    }
  }
}

trait ContractConstructor {
  def value(): String
}


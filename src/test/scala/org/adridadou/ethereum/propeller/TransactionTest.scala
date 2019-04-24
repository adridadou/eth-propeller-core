package org.adridadou.ethereum.propeller


import java.io.File
import java.math.BigInteger

import org.adridadou.ethereum.propeller.backend.{EthereumTest, TestConfig}
import org.adridadou.ethereum.propeller.keystore.AccountProvider
import org.adridadou.ethereum.propeller.solidity.EvmVersion
import org.adridadou.ethereum.propeller.values.EthValue._
import org.adridadou.ethereum.propeller.values._
import org.apache.commons.lang.ArrayUtils
import org.scalatest.check.Checkers
import org.scalatest.{FlatSpec, Matchers}

import scala.compat.java8.OptionConverters._

/**
  * Created by davidroon on 11.04.17.
  * This code is released under Apache 2 license
  */
class TransactionTest extends FlatSpec with Matchers with Checkers {

  private val mainAccount = AccountProvider.fromSeed("Main Test Account")
  private val targetAccount = AccountProvider.fromSeed("Target Test Account")
  private val ethereum = CoreEthereumFacadeProvider.create(new EthereumTest(TestConfig.builder.balance(mainAccount, ether(1000)).build), EthereumConfig.builder().build())

  it should "send the transaction with data and eth to the ethereum network" in {
    val oldBalance = ethereum.getBalance(targetAccount)
    val data = EthData.of("Test: Sending Transaction".getBytes())

    val txDetails = ethereum.sendTx(ether(1), data, mainAccount, targetAccount.getAddress).get()
    txDetails.getTxHash should not be null

    val result = txDetails.getResult.get()
    result.error shouldBe empty
    result.isSuccessful shouldBe true

    ethereum.getBalance(targetAccount) should be(oldBalance.plus(ether(1)))
  }

  it should "estimate the gas usage of the transaction with ether and empty data" in {
    val gasUsage = ethereum.estimateGas(ether(1), EthData.empty, mainAccount, targetAccount.getAddress)

    gasUsage should not be null
    wei(gasUsage.getUsage) should be(wei(221000))
  }

  it should "estimate the gas usage of the transaction with ether and some data" in {
    val data = EthData.of("Test: Sending Transaction".getBytes())

    val gasUsage = ethereum.estimateGas(ether(1), data, mainAccount, targetAccount.getAddress)

    gasUsage should not be null
    wei(gasUsage.getUsage) should be(wei(222700))
  }

  it should "estimate the gas usage of the contract creation transaction" in {
    val contractSource = SoliditySource.from(new File("src/test/resources/contractConstructor.sol"))
    val contract = ethereum.compile(contractSource, new EvmVersion("byzantium")).findContract("ContractConstructor").get
    val constructorArgs = Array("This is a test").map(_.asInstanceOf[Object])
    val smartContract = ethereum.createSmartContract(contract, EthAddress.empty(), mainAccount)
    val argsEncoded: EthData = smartContract
      .getConstructor(constructorArgs).asScala
      .map(constructor => constructor.encode("This is a test")).getOrElse(EthData.empty())

    val data = EthData.of(ArrayUtils.addAll(contract.getBinary.data, argsEncoded.data))

    val gasUsage = ethereum.estimateGas(ether(0), data, mainAccount, EthAddress.empty)

    gasUsage should not be null
    gasUsage.getUsage should be > BigInteger.valueOf(0)
  }

}

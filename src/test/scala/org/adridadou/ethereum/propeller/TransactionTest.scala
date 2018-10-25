package org.adridadou.ethereum.propeller


import org.adridadou.ethereum.propeller.backend.{EthereumTest, TestConfig}
import org.adridadou.ethereum.propeller.keystore.AccountProvider
import org.adridadou.ethereum.propeller.values.EthValue.ether
import org.adridadou.ethereum.propeller.values._
import org.scalatest.check.Checkers
import org.scalatest.{FlatSpec, Matchers}

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
    txDetails.getTxHash should not be (null)

    val result = txDetails.getResult.get()
    result.error shouldBe empty
    result.isSuccessful shouldBe (true)

    ethereum.getBalance(targetAccount) should be (oldBalance.plus(ether(1)))
  }

}

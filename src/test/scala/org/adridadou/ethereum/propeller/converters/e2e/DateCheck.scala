package org.adridadou.ethereum.propeller.converters.e2e

import java.io.File
import java.util.Date

import org.adridadou.ethereum.propeller.CoreEthereumFacadeProvider
import org.adridadou.ethereum.propeller.backend.{EthereumTest, TestConfig}
import org.adridadou.ethereum.propeller.keystore.AccountProvider
import org.adridadou.ethereum.propeller.values.{EthValue, SoliditySourceFile}
import org.scalacheck.Arbitrary._
import org.scalacheck.Prop._
import org.scalatest.check.Checkers
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by davidroon on 26.03.17.
  * This code is released under Apache 2 license
  */
class DateEncoderCheck extends FlatSpec with Matchers with Checkers {

  "Date type" should "be converted from and to date with the same value" in {
    val mainAccount = AccountProvider.fromSeed("test")
    val facade = CoreEthereumFacadeProvider.create(new EthereumTest(TestConfig.builder().balance(mainAccount, EthValue.ether(10000000)).build()))
    val contract = facade.compile(SoliditySourceFile.from(new File("src/test/resources/conversionContract.sol"))).get().findContract("myContract").get()
    val contractAddress = facade.publishContract(contract, mainAccount).get()

    val contractObject = facade.createContractProxy(contract, contractAddress, mainAccount, classOf[DateContract])
    check(forAll(arbitrary[Date])(checkEncode(contractObject, _)))
  }

  private def checkEncode(contractObject: DateContract, date: Date) = {
    contractObject.dateFunc(date) shouldEqual date
    true
  }

}

trait DateContract {
  def dateFunc(dateValue: Date): Date
}

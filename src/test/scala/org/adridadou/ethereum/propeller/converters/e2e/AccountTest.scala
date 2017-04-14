package org.adridadou.ethereum.propeller.converters.e2e

import org.adridadou.ethereum.propeller.values._
import org.scalacheck.Arbitrary._
import org.scalacheck.Prop._
import org.scalatest.check.Checkers
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by davidroon on 26.03.17.
  * This code is released under Apache 2 license
  */
class AccountTest extends FlatSpec with Matchers with Checkers with SolidityConversionHelper {

  "The account type" should "be converted into an address and then encoded and the address should be decoded properly" in {
    val contract = contractObject[AccountContract]
    check(forAll(arbitrary[BigInt])(checkEncode(contract, _)))
  }

  private def checkEncode(contractObject: AccountContract, seed: BigInt) = {
    val account = new EthAccount(seed.bigInteger)
    contractObject.addressFunc(account) shouldEqual account.getAddress
    true
  }

}

trait AccountContract {
  def addressFunc(account: EthAccount): EthAddress
}

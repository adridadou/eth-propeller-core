package org.adridadou.ethereum.propeller.specialtypes

import org.adridadou.ethereum.propeller.converters.e2e.SolidityConversionHelper
import org.adridadou.ethereum.propeller.values._
import org.scalacheck.Arbitrary._
import org.scalacheck.Prop._
import org.scalatest.check.Checkers
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by davidroon on 26.03.17.
  * This code is released under Apache 2 license
  */
class EthPayableCallTest
    extends FlatSpec
    with Matchers
    with Checkers
    with SolidityConversionHelper {

  "The address type" should "be converted from and to address and stay the same value" in {
    val (address, contract) = contractObjectWithAddress[EthPayableContract]
    check(forAll(arbitrary[BigInt])(checkEncode(contract, address, _)))
  }

  private def checkEncode(
      contractObject: EthPayableContract,
      address: EthAddress,
      seed: BigInt
  ) = {
    val account = new EthAccount(seed.bigInteger)
    val beforeBalance = SolidityConversionHelper.facade.getBalance(address)
    contractObject
      .addressPayableFunc(account.getAddress)
      .`with`(EthValue.ether(10))
      .get()
      .getResult
      .get() shouldEqual account.getAddress

    SolidityConversionHelper.facade.getBalance(address) shouldBe beforeBalance
      .plus(EthValue.ether(10))

    true
  }
}

trait EthPayableContract {
  def addressPayableFunc(account: EthAddress): EthPayableCall[EthAddress]
}

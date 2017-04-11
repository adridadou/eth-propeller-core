package org.adridadou.ethereum.propeller.converters.encoders

import org.adridadou.ethereum.propeller.solidity.converters.encoders.AddressEncoder
import org.adridadou.ethereum.propeller.values.{EthAccount, EthData}
import org.ethereum.solidity.SolidityType
import org.junit.Test
import org.scalacheck.Arbitrary._
import org.scalacheck.Prop._
import org.scalatest.Matchers
import org.scalatest.check.Checkers

/**
  * Created by davidroon on 26.03.17.
  * This code is released under Apache 2 license
  */
class AddressEncoderCheck extends Checkers with Matchers {

  @Test
  def checkEncode(): Unit = {
    check(forAll(arbitrary[BigInt])(checkEncode))
  }

  private def checkEncode(seed: BigInt) = {
    val account = new EthAccount(seed.bigInteger)
    val ethjEncoder = new SolidityType.AddressType()
    val propellerEncoder = new AddressEncoder()
    val ethjResult = EthData.of(ethjEncoder.encode(account.getAddress.address))
    val propellerResult = propellerEncoder.encode(account.getAddress)

    ethjResult shouldEqual propellerResult

    true
  }

}

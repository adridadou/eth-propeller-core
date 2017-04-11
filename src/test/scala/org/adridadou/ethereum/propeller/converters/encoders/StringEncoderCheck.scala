package org.adridadou.ethereum.propeller.converters.encoders


import org.adridadou.ethereum.propeller.solidity.converters.encoders.StringEncoder
import org.adridadou.ethereum.propeller.values.EthData
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
class StringEncoderCheck extends Checkers with Matchers {

  @Test
  def checkEncode(): Unit = {
    check(forAll(arbitrary[String])(checkEncode))
  }

  private def checkEncode(str: String) = {
    val ethjEncoder = new SolidityType.StringType()
    val propellerEncoder = new StringEncoder()
    val ethjResult = EthData.of(ethjEncoder.encode(str))
    val propellerResult = propellerEncoder.encode(str)

    ethjResult shouldEqual propellerResult

    true
  }

}

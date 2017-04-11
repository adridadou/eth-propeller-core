package org.adridadou.ethereum.propeller.converters.encoders

import org.adridadou.ethereum.propeller.solidity.converters.encoders.NumberEncoder
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
class NumberEncoderCheck extends Checkers with Matchers {

  @Test
  def checkEncode(): Unit = {
    check(forAll(arbitrary[BigInt])(checkEncode))
  }

  private def checkEncode(seed: BigInt) = {
    val ethjEncoder = new SolidityType.IntType("int")
    val propellerEncoder = new NumberEncoder()
    EthData.of(ethjEncoder.encode(seed.bigInteger.intValue())) shouldEqual propellerEncoder.encode(seed.bigInteger.intValue())
    EthData.of(ethjEncoder.encode(seed.bigInteger.longValue())) shouldEqual propellerEncoder.encode(seed.bigInteger.longValue())
    EthData.of(ethjEncoder.encode(seed.bigInteger)) shouldEqual propellerEncoder.encode(seed.bigInteger)

    true
  }

}

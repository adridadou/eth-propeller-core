package org.adridadou.ethereum.propeller.converters.e2e

import java.util.Date

import org.adridadou.ethereum.propeller.solidity.converters.encoders.DateEncoder
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
class DateEncoderCheck extends Checkers with Matchers {

  @Test
  def checkEncode(): Unit = {
    check(forAll(arbitrary[Date])(checkEncode))
  }

  private def checkEncode(date: Date) = {
    val ethjEncoder = new SolidityType.IntType("int")
    val propellerEncoder = new DateEncoder()
    val ethjResult = EthData.of(ethjEncoder.encode(date.getTime))
    val propellerResult = propellerEncoder.encode(date)

    ethjResult shouldEqual propellerResult

    true
  }

}

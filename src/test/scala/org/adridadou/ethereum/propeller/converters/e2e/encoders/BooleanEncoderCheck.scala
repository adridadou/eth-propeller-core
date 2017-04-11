package org.adridadou.ethereum.propeller.converters.e2e.encoders

import org.adridadou.ethereum.propeller.solidity.converters.encoders.BooleanEncoder
import org.adridadou.ethereum.propeller.values.EthData
import org.ethereum.solidity.SolidityType
import org.junit.Test
import org.scalatest.Matchers
import org.scalatest.check.Checkers

/**
  * Created by davidroon on 26.03.17.
  * This code is released under Apache 2 license
  */
class BooleanEncoderCheck extends Checkers with Matchers {

  @Test
  def checkEncode(): Unit = {
    checkEncode(true)
    checkEncode(false)
  }

  private def checkEncode(bool: Boolean) = {
    val ethjEncoder = new SolidityType.BoolType()
    val propellerEncoder = new BooleanEncoder()
    val ethjResult = EthData.of(ethjEncoder.encode(bool))
    val propellerResult = propellerEncoder.encode(bool)

    ethjResult shouldEqual propellerResult

    true
  }

}

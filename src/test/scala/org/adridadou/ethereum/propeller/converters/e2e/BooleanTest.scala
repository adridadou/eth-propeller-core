package org.adridadou.ethereum.propeller.converters.e2e

import org.scalatest.check.Checkers
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by davidroon on 26.03.17.
  * This code is released under Apache 2 license
  */
class BooleanTest
    extends FlatSpec
    with Matchers
    with Checkers
    with SolidityConversionHelper {
  "boolean type" should "be converted from and to boolean with the same value" in {
    val contract = contractObject[BoolContract]
    checkEncode(contract, value = true)
    checkEncode(contract, value = false)
  }

  private def checkEncode(contractObject: BoolContract, value: Boolean) = {
    contractObject.boolFunc(value) shouldEqual value
    true
  }

}

trait BoolContract {
  def boolFunc(value: Boolean): Boolean
}

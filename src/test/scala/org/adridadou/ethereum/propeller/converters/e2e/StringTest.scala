package org.adridadou.ethereum.propeller.converters.e2e

import org.scalacheck.Arbitrary._
import org.scalacheck.Prop._
import org.scalatest.check.Checkers
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by davidroon on 26.03.17.
  * This code is released under Apache 2 license
  */
class StringTest extends FlatSpec with Matchers with Checkers with SolidityConversionHelper {

  "String type" should "be converted from and to the same value" in {
    val contract = contractObject[StringContract]
    check(forAll(arbitrary[String])(checkEncode(contract, _)))
  }

  private def checkEncode(contractObject: StringContract, str: String) = {
    contractObject.strFunc(str) shouldEqual str
    true
  }
}

trait StringContract {
  def strFunc(strValue: String): String
}

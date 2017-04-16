package org.adridadou.ethereum.propeller.converters.e2e

import java.math.BigInteger

import org.scalacheck.Arbitrary._
import org.scalacheck.Prop._
import org.scalatest.check.Checkers
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by davidroon on 26.03.17.
  * This code is released under Apache 2 license
  */
class MixWithStringTest extends FlatSpec with Matchers with Checkers with SolidityConversionHelper {

  "Multiple parameters" should "convert correctly" in {
    val contract = contractObject[MixContract]
    check(forAll(arbitrary[BigInt], arbitrary[String], arbitrary[Boolean], arbitrary[String])(checkEncode(contract, _, _, _, _)))
  }

  private def checkEncode(contract: MixContract, bi1: BigInt, str1: String, bi2: Boolean, str2: String) = {
    contract.mixWithStringFunc(bi1.bigInteger, str1, bi2, str2) shouldEqual str2
    true
  }
}

trait MixContract {
  def mixWithStringFunc(test1: BigInteger, test2: String, test3: Boolean, test4: String): String
}

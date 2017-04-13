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
class NumberTest extends FlatSpec with Matchers with Checkers with SolidityConversionHelper {

  "Number type (bigInteger, int, long)" should "be converted from and to the same value" in {
    check(forAll(arbitrary[BigInt])(checkEncode(contractObject[NumberContract](), _)))
  }

  private def checkEncode(contract: NumberContract, seed: BigInt) = {
    contract.intFunc(seed.bigInteger.intValue()) shouldEqual seed.bigInteger.intValue()
    contract.intFunc(seed.bigInteger.longValue()) shouldEqual seed.bigInteger.longValue()
    contract.intFunc(seed.bigInteger) shouldEqual seed.bigInteger
    true
  }

}

trait NumberContract {
  def intFunc(intValue: BigInteger): BigInteger

  def intFunc(intValue: Int): Integer

  def intFunc(intValue: Long): Long
}

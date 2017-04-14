package org.adridadou.ethereum.propeller.converters.e2e

import java.math.BigInteger

import org.scalacheck.Arbitrary._
import org.scalacheck.Prop._
import org.scalatest.check.Checkers
import org.scalatest.{FlatSpec, Matchers}

import scala.util.Try

/**
  * Created by davidroon on 26.03.17.
  * This code is released under Apache 2 license
  */
class NumberTest extends FlatSpec with Matchers with Checkers with SolidityConversionHelper {

  "Number type" should "convert big integer from and to the same value" in {
    check(forAll(arbitrary[BigInt])(checkEncode(contractObject[NumberContract](), _)))
  }

  it should "convert Integer from and to the same value" in {
    check(forAll(arbitrary[Int])(checkEncode(contractObject[NumberContract](), _)))
  }

  it should "convert Long from and to the same value" in {
    check(forAll(arbitrary[Long])(checkEncode(contractObject[NumberContract](), _)))
  }

  private def checkEncode(contract: NumberContract, seed: Long) = {
    if (seed < 0) {
      Try(contract.intFunc(seed)).isFailure shouldEqual true
    } else {
      contract.intFunc(seed) shouldEqual seed
    }

    true
  }

  private def checkEncode(contract: NumberContract, seed: Int) = {
    if (seed < 0) {
      Try(contract.intFunc(seed.asInstanceOf[Integer])).isFailure shouldEqual true
    } else {
      contract.intFunc(seed.asInstanceOf[Integer]) shouldEqual seed
    }

    true
  }

  private def checkEncode(contract: NumberContract, seed: BigInt) = {
    val biValue = seed.bigInteger
    if (biValue.signum() == -1) {
      Try(contract.intFunc(biValue)).isFailure shouldEqual true
    } else {
      contract.intFunc(biValue) shouldEqual biValue
    }

    true
  }

}

trait NumberContract {
  def intFunc(intValue: BigInteger): BigInteger

  def intFunc(intValue: Integer): Integer

  def intFunc(intValue: java.lang.Long): java.lang.Long
}

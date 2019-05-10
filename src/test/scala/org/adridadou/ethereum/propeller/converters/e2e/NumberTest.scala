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
class NumberTest
    extends FlatSpec
    with Matchers
    with Checkers
    with SolidityConversionHelper {

  private val contract = contractObject[NumberContract]

  "Number type" should "convert big integer from and to the same value" in {
    check(forAll(arbitrary[BigInt])(checkEncode(contract, _)))
  }

  it should "convert Integer from and to the same value" in {
    check(forAll(arbitrary[Int])(checkEncode(contract, _)))
  }

  it should "convert Long from and to the same value" in {
    check(forAll(arbitrary[Long])(checkEncode(contract, _)))
  }

  it should "convert byte from and to the same value" in {
    check(forAll(arbitrary[Byte])(checkEncodeSmall(contract, _)))
  }

  private def checkEncode(contract: NumberContract, seed: Long) = {
    if (seed < 0) {
      Try(contract.uintFunc(seed)).isFailure shouldEqual true
    } else {
      contract.uintFunc(seed) shouldEqual seed
    }

    contract.intFunc(seed) shouldEqual seed

    true
  }

  private def checkEncode(contract: NumberContract, seed: Int) = {
    if (seed < 0) {
      Try(contract.uintFunc(seed.asInstanceOf[Integer])).isFailure shouldEqual true
    } else {
      contract.uintFunc(seed.asInstanceOf[Integer]) shouldEqual seed
    }

    contract.intFunc(seed.asInstanceOf[Integer]) shouldEqual seed

    true
  }

  private def checkEncodeSmall(contract: NumberContract, seed: Byte) = {
    if (seed < 0) {
      Try(contract.smallUintFunc(seed.asInstanceOf[java.lang.Byte])).isFailure shouldEqual true
    } else {
      contract.smallUintFunc(seed.asInstanceOf[java.lang.Byte]) shouldEqual seed
    }

    true
  }

  private def checkEncode(contract: NumberContract, seed: BigInt) = {
    val biValue = seed.bigInteger
    if (biValue.signum() == -1) {
      Try(contract.uintFunc(biValue)).isFailure shouldEqual true
    } else {
      contract.uintFunc(biValue) shouldEqual biValue
    }

    contract.intFunc(biValue) shouldEqual biValue

    true
  }

}

trait NumberContract {
  def intFunc(intValue: BigInteger): BigInteger

  def intFunc(intValue: Integer): Integer

  def intFunc(intValue: java.lang.Long): java.lang.Long

  def uintFunc(intValue: BigInteger): BigInteger

  def uintFunc(intValue: Integer): Integer

  def uintFunc(intValue: java.lang.Long): java.lang.Long

  def smallUintFunc(byteValue: java.lang.Byte): java.lang.Byte
}

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
class ComplexReturnTypeTest extends FlatSpec with Matchers with Checkers with SolidityConversionHelper {

  "ComplexReturnTypeTest" should "convert to a class" in {
    val contract = contractObject[ComplexReturnTypeContract]
    check(forAll(arbitrary[BigInt], arbitrary[String], arbitrary[Boolean], arbitrary[String])(checkEncode(contract, _, _, _, _)))
  }

  private def checkEncode(contract: ComplexReturnTypeContract, bi1: BigInt, str1: String, bi2: Boolean, str2: String) = {
    contract.complexReturnType(bi1.bigInteger, str1, bi2, str2) shouldEqual new MyReturnType(bi1.bigInteger, str1, bi2, str2)

    true
  }
}

trait ComplexReturnTypeContract {
  def complexReturnType(test1: BigInteger, test2: String, test3: Boolean, test4: String): MyReturnType
}


class MyReturnType(val val1: BigInteger, val val2: String, val val3: Boolean, val val4: String) {

  override def equals(other: Any): Boolean = other match {
    case that: MyReturnType =>
      (that canEqual this) &&
        val1 == that.val1 &&
        val2 == that.val2 &&
        val3 == that.val3 &&
        val4 == that.val4
    case _ => false
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[MyReturnType]

  override def hashCode(): Int = {
    val state = Seq(val1, val2, val3, val4)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}
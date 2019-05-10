package org.adridadou.ethereum.propeller.converters.e2e

import java.math.BigInteger

import org.scalacheck.Arbitrary._
import org.scalacheck.Prop._
import org.scalatest.check.Checkers
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by davidroon on 26.03.18.
  * This code is released under Apache 2 license
  */
class ListTest
    extends FlatSpec
    with Matchers
    with Checkers
    with SolidityConversionHelper {

  "List type" should "be converted from and to the same value" in {
    val contract = contractObject[ListContract]
    check(forAll(arbitrary[Array[Int]])(checkEncode(contract, _)))
  }

  private def checkEncode(contractObject: ListContract, value: Array[Int]) = {
    val realValue = value.map(i => BigInt(i).abs.bigInteger)
    contractObject.lstFunc(realValue) shouldEqual realValue
    true
  }
}

trait ListContract {
  def lstFunc(value: Array[BigInteger]): Array[BigInteger]
}

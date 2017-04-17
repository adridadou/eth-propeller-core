package org.adridadou.ethereum.propeller.converters.e2e

import java.math.BigInteger
import java.util

import org.scalacheck.Arbitrary._
import org.scalacheck.Prop._
import org.scalatest.check.Checkers
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.JavaConverters._
import scala.util.Try

/**
  * Created by davidroon on 26.03.17.
  * This code is released under Apache 2 license
  */
class CollectionTest extends FlatSpec with Matchers with Checkers with SolidityConversionHelper {

  "Collection type (array, list, set)" should "be converted from and to the same value" in {
    val contract = contractObject[CollectionContract]
    check(forAll(arbitrary[Array[BigInt]])(checkEncode(contract, _)))
  }

  private def checkEncode(contract: CollectionContract, s: Seq[BigInt]) = {
    val seed = s.map(_.bigInteger)
    if (s.size > 10) {
      Try(contract.arrayFunc(seed.toArray)).isFailure shouldEqual true
    } else {
      val result = createResult(seed)
      contract.arrayFunc(seed.toArray) shouldEqual result.toArray
      contract.arrayFunc(seed.asJava) shouldEqual result.asJava
      contract.arrayFunc(new util.HashSet(seed.asJava)) shouldEqual new util.HashSet(result.asJava)
    }

    contract.dynArrayFunc(seed.asJava) shouldEqual getDynResult(seed.asJava)

    true
  }

  private def getDynResult(seed: java.util.List[BigInteger]) = {
    if (seed.size > 3) seed.get(3) else BigInteger.ZERO
  }

  private def createResult(seed: Seq[BigInteger]): Seq[BigInteger] = {
    if (seed.size > 9) {
      seed
    } else {
      val zeroTail = (0 until (10 - seed.size)).map(i => BigInteger.ZERO)
      seed ++ zeroTail
    }
  }

}

trait CollectionContract {
  def arrayFunc(intValue: java.util.List[BigInteger]): java.util.List[BigInteger]

  def arrayFunc(intValue: Array[BigInteger]): Array[BigInteger]

  def arrayFunc(intValue: java.util.Set[BigInteger]): java.util.Set[BigInteger]

  def dynArrayFunc(intValue: java.util.List[BigInteger]): BigInteger
}

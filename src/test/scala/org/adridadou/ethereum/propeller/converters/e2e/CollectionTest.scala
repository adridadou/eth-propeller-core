package org.adridadou.ethereum.propeller.converters.e2e

import java.math.BigInteger
import java.util

import org.scalatest.check.Checkers
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.JavaConverters._

/**
  * Created by davidroon on 26.03.17.
  * This code is released under Apache 2 license
  */
class CollectionTest extends FlatSpec with Matchers with Checkers with SolidityConversionHelper {

  "Collection type (array, list, set)" should "be converted from and to the same value" in {
    val contract = contractObject[CollectionContract]
    //check(forAll(arbitrary[Array[BigInt]])(checkEncode(contract, _)))
    checkEncode(contract, Array(BigInt(1)))
  }

  private def checkEncode(contract: CollectionContract, s: Seq[BigInt]) = {
    val seed = s.map(_.bigInteger)
    val result = createResult(seed)
    contract.arrayFunc(seed.toArray) shouldEqual result.toArray
    contract.arrayFunc(seed.asJava) shouldEqual result.asJava
    contract.arrayFunc(new util.HashSet(seed.asJava)) shouldEqual new util.HashSet(result.asJava)
    true
  }

  private def createResult(seed: Seq[BigInteger]): Seq[BigInteger] = {
    if (seed.size > 9) return seed
    val zeroTail = (0 until (10 - seed.size)).map(i => BigInteger.ZERO)
    seed ++ zeroTail
  }

}

trait CollectionContract {
  def arrayFunc(intValue: java.util.List[BigInteger]): java.util.List[BigInteger]

  def arrayFunc(intValue: Array[BigInteger]): Array[BigInteger]

  def arrayFunc(intValue: java.util.Set[BigInteger]): java.util.Set[BigInteger]
}

package org.adridadou.ethereum.propeller.converters.e2e

import org.adridadou.ethereum.propeller.values.EthValue
import org.scalacheck.Arbitrary._
import org.scalacheck.Prop._
import org.scalatest.check.Checkers
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by davidroon on 26.03.17.
  * This code is released under Apache 2 license
  */
class EthValueTest
    extends FlatSpec
    with Matchers
    with Checkers
    with SolidityConversionHelper {

  private val contract = contractObject[EthValueContract]

  "Value type" should "convert EthValue from and to the same value" in {
    check(forAll(arbitrary[BigInt])(checkEncode(contract, _)))
  }

  private def checkEncode(contract: EthValueContract, seed: BigInt) = {
    val biValue = seed.bigInteger
    if (biValue.signum() > -1) {
      val value = EthValue.wei(biValue)
      contract.uintFunc(value) shouldEqual value
    }
    true
  }

}

trait EthValueContract {
  def uintFunc(intValue: EthValue): EthValue
}

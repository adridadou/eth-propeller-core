package org.adridadou.ethereum.propeller.converters.e2e

import org.adridadou.ethereum.propeller.values.{EthData, EthSignature}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Prop.forAll
import org.scalatest.check.Checkers
import org.scalatest.{FlatSpec, Matchers}


/**
  * Created by davidroon on 26.03.17.
  * This code is released under Apache 2 license
  */
class RawTest extends FlatSpec with Matchers with Checkers with SolidityConversionHelper {

  private val contract = contractObject[RawContract]

  "Raw types" should "convert from and to the same value" in {
    check(forAll(arbitrary[Array[Byte]])(checkEncode(contract, _)))
  }

  private def checkEncode(contract: RawContract, seed: Array[Byte]) = {
    val data = EthData.of(seed)
    if (seed.length > 32) {
      contract.bytesFunc(data) shouldEqual data
    } else {
      contract.bytes32Func(data) shouldEqual data.word(0)
    }

    val signature = SolidityConversionHelper.mainAccount.sign(data)

    contract.signatureFunc(signature) shouldBe signature

    true
  }
}

trait RawContract {
  def bytes32Func(value: EthData): EthData

  def bytesFunc(value: EthData): EthData

  def signatureFunc(signature: EthSignature): EthSignature
}



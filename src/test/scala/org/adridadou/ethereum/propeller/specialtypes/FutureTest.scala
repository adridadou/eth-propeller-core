package org.adridadou.ethereum.propeller.specialtypes

import java.util.concurrent.CompletableFuture

import org.adridadou.ethereum.propeller.converters.e2e.SolidityConversionHelper
import org.adridadou.ethereum.propeller.values._
import org.scalacheck.Arbitrary._
import org.scalacheck.Prop._
import org.scalatest.check.Checkers
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by davidroon on 26.03.17.
  * This code is released under Apache 2 license
  */
class FutureTest
    extends FlatSpec
    with Matchers
    with Checkers
    with SolidityConversionHelper {

  "The address type" should "be converted from and to address and stay the same value" in {
    val contract = contractObject[FutureContract]
    check(forAll(arbitrary[BigInt])(checkEncode(contract, _)))
  }

  private def checkEncode(contractObject: FutureContract, seed: BigInt) = {
    val account = new EthAccount(seed.bigInteger)
    contractObject
      .addressFunc(account.getAddress)
      .get() shouldEqual account.getAddress

    true
  }

}

trait FutureContract {
  def addressFunc(account: EthAddress): CompletableFuture[EthAddress]
}

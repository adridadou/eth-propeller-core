package org.adridadou.ethereum.propeller.values

import org.ethereum.crypto.ECKey
import org.junit.Test
import org.scalacheck.Arbitrary._
import org.scalacheck.Prop._
import org.scalatest.Matchers
import org.scalatest.check.Checkers

/**
  * Created by davidroon on 26.03.17.
  * This code is released under Apache 2 license
  */
class EthAccountCheck extends Checkers with Matchers {

  @Test
  def checkGenerateSamePublicKeyAndAddress(): Unit = {
    check(forAll(arbitrary[BigInt])(checkSameAddressGenerated))
  }

  private def checkSameAddressGenerated(seed: BigInt) = {
    val ethjVersion = ECKey.fromPrivate(seed.bigInteger)
    val propellerVersion = new EthAccount(seed.bigInteger)
    val ethjAddress = EthAddress.of(ethjVersion.getAddress)

    ethjVersion.getPubKeyPoint shouldEqual propellerVersion.getPublicKey
    ethjAddress shouldEqual propellerVersion.getAddress
    true
  }

}

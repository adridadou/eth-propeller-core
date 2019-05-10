package org.adridadou.ethereum.propeller.values

import org.adridadou.ethereum.propeller.keystore.AccountProvider
import org.ethereum.crypto.ECKey
import org.scalacheck.Arbitrary._
import org.scalacheck.Prop._
import org.scalatest.check.Checkers
import org.scalatest.{Matchers, _}

import scala.util.{Failure, Success, Try}

/**
  * Created by davidroon on 26.03.17.
  * This code is released under Apache 2 license
  */
class EthAccountTest extends FlatSpec with Matchers with Checkers {

  "An ethereum account" should "generate the same key than the one in ethereumJ if given a specific seed" in {
    check(forAll(arbitrary[BigInt])(checkSameAddressGenerated))
  }

  it should "be able to generate and then recover from a random account" in {
    val account = AccountProvider.random()
    val data = account.getDataPrivateKey
    val account2 = AccountProvider.fromPrivateKey(data)

    account.getAddress shouldEqual account2.getAddress
  }

  private def checkSameAddressGenerated(seed: BigInt) = {
    if (seed === 0) {
      Try(ECKey.fromPrivate(seed.bigInteger)) match {
        case Success(_) =>
          throw new RuntimeException(
            "it should not be possible to create a private key from int 0"
          )
        case Failure(ex) =>
          ex.getMessage shouldEqual "Public key must not be a point at infinity, probably your private key is incorrect"
          true
      }
    } else {
      val ethjVersion = ECKey.fromPrivate(seed.bigInteger)
      val propellerVersion = new EthAccount(seed.bigInteger)
      val ethjAddress = EthAddress.of(ethjVersion.getAddress)

      ethjVersion.getPubKeyPoint shouldEqual propellerVersion.getPublicKey
      ethjAddress shouldEqual propellerVersion.getAddress
      true
    }
  }

}

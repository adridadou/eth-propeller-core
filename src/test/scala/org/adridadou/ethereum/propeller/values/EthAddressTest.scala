package org.adridadou.ethereum.propeller.values

import org.scalacheck.Arbitrary._
import org.scalacheck.Prop._
import org.scalatest.check.Checkers
import org.scalatest.{Matchers, _}
import org.spongycastle.util.encoders.Hex

import scala.util.{Failure, Success, Try}

/**
  * Created by davidroon on 26.03.17.
  * This code is released under Apache 2 license
  */
class EthAddressTest extends FlatSpec with Matchers with Checkers {

  "An ethereum address" should "handle byte arrays of any length as long as it is up to 40 bytes" in {
    check(forAll(arbitrary[Array[Byte]])(checkSameAddressGenerated))
  }

  private def checkSameAddressGenerated(a: Array[Byte]) = {
    val array: Array[Byte] = a.map(elem => Math.abs(elem).toByte)
    if (EthAddress.trimLeft(array).length > EthAddress.MAX_ADDRESS_SIZE) {
      Try(EthAddress.of(array)) match {
        case Success(_) => fail("should fail")
        case Failure(ex) => ex.getMessage shouldEqual "byte array of the address cannot be bigger than 20.value:" + Hex.toHexString(EthAddress.trimLeft(array))
      }
    } else {
      val address = EthAddress.of(array)
      address.normalizedString().length shouldEqual 40
      address.address should contain theSameElementsAs EthAddress.trimLeft(array)
    }
    true
  }

}

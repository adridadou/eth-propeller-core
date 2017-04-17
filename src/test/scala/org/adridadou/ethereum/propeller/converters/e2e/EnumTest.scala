package org.adridadou.ethereum.propeller.converters.e2e

import org.adridadou.ethereum.propeller.MyEnum
import org.scalatest.check.Checkers
import org.scalatest.{FlatSpec, Matchers}


/**
  * Created by davidroon on 26.03.17.
  * This code is released under Apache 2 license
  */
class EnumTest extends FlatSpec with Matchers with Checkers with SolidityConversionHelper {

  val contract = contractObject[EnumContract]

  "Enum type" should "convert from and to the same value" in {
    MyEnum.values().foreach(checkEncode(contract, _))
  }

  private def checkEncode(contract: EnumContract, seed: MyEnum) = {
    contract.intFunc(seed) shouldEqual seed
    true
  }
}

trait EnumContract {
  def intFunc(intValue: MyEnum): MyEnum
}



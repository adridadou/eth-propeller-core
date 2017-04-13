package org.adridadou.ethereum.propeller.converters.e2e

import java.util.Date

import org.scalacheck.Arbitrary._
import org.scalacheck.Prop._
import org.scalatest.check.Checkers
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by davidroon on 26.03.17.
  * This code is released under Apache 2 license
  */
class DateEncoderTest extends FlatSpec with Matchers with Checkers with SolidityConversionHelper {

  "Date type" should "be converted from and to date with the same value" in {
    check(forAll(arbitrary[Date])(checkEncode(contractObject[DateContract], _)))
  }

  private def checkEncode(contractObject: DateContract, date: Date) = {
    contractObject.dateFunc(date) shouldEqual date
    true
  }

}

trait DateContract {
  def dateFunc(dateValue: Date): Date
}

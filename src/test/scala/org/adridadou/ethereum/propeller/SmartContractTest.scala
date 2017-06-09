package org.adridadou.ethereum.propeller

import java.io.File

import org.adridadou.ethereum.propeller.backend.{EthereumTest, TestConfig}
import org.adridadou.ethereum.propeller.exception.EthereumApiException
import org.adridadou.ethereum.propeller.keystore.AccountProvider
import org.adridadou.ethereum.propeller.values.EthValue.ether
import org.adridadou.ethereum.propeller.values._
import org.scalatest.check.Checkers
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.JavaConverters._
import scala.compat.java8.OptionConverters._

/**
  * Created by davidroon on 11.04.17.
  * This code is released under Apache 2 license
  */
class SmartContractTest extends FlatSpec with Matchers with Checkers {

  private val mainAccount = AccountProvider.fromSeed("hello")
  private val ethereum = CoreEthereumFacadeProvider.create(new EthereumTest(TestConfig.builder.balance(mainAccount, ether(1000)).build), EthereumConfig.builder().build())
  private val contractSource = SoliditySource.from(new File("src/test/resources/contractEvents.sol"))

  "Events" should "be observable from the ethereum network" in {
    val address = publishAndMapContract(ethereum)
    (for (compiledContract <- ethereum.compile(contractSource).findContract("contractEvents").asScala;
          solidityEvent <- ethereum.findEventDefinition(compiledContract, "MyEvent", classOf[MyEvent]).asScala) yield {
      val myContract = ethereum.createSmartContract(compiledContract, address, mainAccount)
      val observeEventWithInfo = ethereum.observeEventsWithInfo(solidityEvent, address)

      val func = myContract.getFunctions.asScala.find(_.getName.equals("createEvent")).get

      myContract.callFunctionAndGetDetails(EthValue.wei(0), func, "my event is here and it is much longer than anticipated")
      val result = observeEventWithInfo.toBlocking.first()

      result.getTransactionHash shouldBe EthHash.of("6717c8616d06184e589aae321d1a2349679675fc6c7af95368c2e5f3e71daaef")
      result.getResult.value shouldBe "my event is here and it is much longer than anticipated"

    }).getOrElse(() => throw new EthereumApiException("something went wrong!"))
  }

  private def publishAndMapContract(ethereum: EthereumFacade) = {
    val compiledContract = ethereum.compile(contractSource).findContract("contractEvents").get
    val futureAddress = ethereum.publishContract(compiledContract, mainAccount)
    futureAddress.get
  }
}

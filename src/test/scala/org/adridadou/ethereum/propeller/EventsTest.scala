package org.adridadou.ethereum.propeller

import java.io.File
import java.util.concurrent.CompletableFuture

import org.adridadou.ethereum.propeller.backend.{EthereumTest, TestConfig}
import org.adridadou.ethereum.propeller.exception.EthereumApiException
import org.adridadou.ethereum.propeller.keystore.AccountProvider
import org.adridadou.ethereum.propeller.values.EthValue.ether
import org.adridadou.ethereum.propeller.values.SoliditySource
import org.junit.Assert.assertEquals
import org.scalatest.check.Checkers
import org.scalatest.{FlatSpec, Matchers}

import scala.compat.java8.OptionConverters._

/**
  * Created by davidroon on 11.04.17.
  * This code is released under Apache 2 license
  */
class EventsTest extends FlatSpec with Matchers with Checkers {

  private val mainAccount = AccountProvider.fromSeed("hello")
  private val ethereum = CoreEthereumFacadeProvider.create(new EthereumTest(TestConfig.builder.balance(mainAccount, ether(1000)).build), new EthereumConfig())
  private val contractSource = SoliditySource.from(new File("src/test/resources/contractEvents.sol"))

  "Events" should "be observable from the ethereum network" in {
    val address = publishAndMapContract(ethereum)
    (for (compiledContract <- ethereum.compile(contractSource).findContract("contractEvents").asScala;
          solidityEvent <- ethereum.findEventDefinition(compiledContract, "MyEvent", classOf[MyEvent]).asScala) yield {
      val myContract = ethereum.createContractProxy(compiledContract, address, mainAccount, classOf[ContractEvents])
      val observeEvent = ethereum.observeEvents(solidityEvent, address, classOf[MyEvent])
      myContract.createEvent("my event is here")
      assertEquals("my event is here", observeEvent.toBlocking.first.value)
    }).getOrElse(() => throw new EthereumApiException("something went wrong!"))
  }

  private def publishAndMapContract(ethereum: EthereumFacade) = {
    val compiledContract = ethereum.compile(contractSource).findContract("contractEvents").get
    val futureAddress = ethereum.publishContract(compiledContract, mainAccount)
    futureAddress.get
  }
}

trait ContractEvents {
  def createEvent(value: String): CompletableFuture[Void]
}

class MyEvent(val value: String) {
  override def toString: String = "MyEvent{" + "value='" + value + '\'' + '}'
}

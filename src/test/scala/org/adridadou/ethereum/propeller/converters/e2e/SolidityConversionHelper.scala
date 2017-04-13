package org.adridadou.ethereum.propeller.converters.e2e

import java.io.File

import org.adridadou.ethereum.propeller.CoreEthereumFacadeProvider
import org.adridadou.ethereum.propeller.backend.{EthereumTest, TestConfig}
import org.adridadou.ethereum.propeller.keystore.AccountProvider
import org.adridadou.ethereum.propeller.values.{EthValue, SoliditySourceFile}

import scala.reflect.ClassTag

/**
  * Created by davidroon on 13.04.17.
  * This code is released under Apache 2 license
  */
trait SolidityConversionHelper {
  def contractObject[T]()(implicit tag: ClassTag[T]): T = {
    val mainAccount = AccountProvider.fromSeed("test")
    val facade = CoreEthereumFacadeProvider.create(new EthereumTest(TestConfig.builder().balance(mainAccount, EthValue.ether(10000000)).build()))
    val contract = facade.compile(SoliditySourceFile.from(new File("src/test/resources/conversionContract.sol"))).get().findContract("myContract").get()
    val contractAddress = facade.publishContract(contract, mainAccount).get()

    facade.createContractProxy(contract, contractAddress, mainAccount, tag.runtimeClass).asInstanceOf[T]
  }

}

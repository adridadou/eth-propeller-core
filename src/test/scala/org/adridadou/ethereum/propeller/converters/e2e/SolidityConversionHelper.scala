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

object SolidityConversionHelper {
  lazy val mainAccount = AccountProvider.fromSeed("test")
  lazy val facade = CoreEthereumFacadeProvider.create(new EthereumTest(TestConfig.builder().balance(mainAccount, EthValue.ether(10000000)).build()))
  lazy val contract = facade.compile(SoliditySourceFile.from(new File("src/test/resources/conversionContract.sol"))).get().findContract("myContract").get()
}

trait SolidityConversionHelper {
  def contractObject[T]()(implicit tag: ClassTag[T]): T = {
    val contractAddress = SolidityConversionHelper.facade.publishContract(SolidityConversionHelper.contract, SolidityConversionHelper.mainAccount).get()
    SolidityConversionHelper.facade.createContractProxy(SolidityConversionHelper.contract, contractAddress, SolidityConversionHelper.mainAccount, tag.runtimeClass).asInstanceOf[T]
  }

}

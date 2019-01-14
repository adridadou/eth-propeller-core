package org.adridadou.ethereum.propeller.converters.e2e

import java.io.File

import org.adridadou.ethereum.propeller.backend.{EthereumTest, TestConfig}
import org.adridadou.ethereum.propeller.keystore.AccountProvider
import org.adridadou.ethereum.propeller.solidity.SolidityContractDetails
import org.adridadou.ethereum.propeller.values.{EthAccount, EthAddress, EthValue, SoliditySourceFile}
import org.adridadou.ethereum.propeller.{CoreEthereumFacadeProvider, EthereumConfig, EthereumFacade}

import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

/**
  * Created by davidroon on 13.04.17.
  * This code is released under Apache 2 license
  */

object SolidityConversionHelper {
  val mainAccount: EthAccount = AccountProvider.fromSeed("test")
  val facade: EthereumFacade = CoreEthereumFacadeProvider
    .create(new EthereumTest(TestConfig.builder()
      .balance(mainAccount, EthValue.ether(10000000))
      .build()), EthereumConfig.builder().build())

  val contract: SolidityContractDetails = SolidityConversionHelper.facade.compile(SoliditySourceFile.from(new File("src/test/resources/conversionContract.sol")))
    .findContract("myContract").get()

  val contractAddress: EthAddress = facade.publishContract(contract, mainAccount).get()
}

trait SolidityConversionHelper {

  def contractObject[T]()(implicit tag: ClassTag[T]): T = {
    Try(SolidityConversionHelper.facade
      .createContractProxy(
        SolidityConversionHelper.contract,
        SolidityConversionHelper.contractAddress,
        SolidityConversionHelper.mainAccount,
        tag.runtimeClass).asInstanceOf[T]) match {
      case Success(result) => result
      case Failure(ex) =>
        throw ex
    }
  }

  def contractObjectWithAddress[T]()(implicit tag: ClassTag[T]): (EthAddress, T) = {
    val result = (SolidityConversionHelper.contractAddress, SolidityConversionHelper.facade.createContractProxy(SolidityConversionHelper.contract, SolidityConversionHelper.contractAddress, SolidityConversionHelper.mainAccount, tag.runtimeClass).asInstanceOf[T])

    result
  }
}

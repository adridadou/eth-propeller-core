package org.adridadou.ethereum.propeller

import java.util.concurrent.{CompletableFuture, TimeUnit}
import java.util.function.Consumer

import io.reactivex.{BackpressureStrategy, Observable}
import io.reactivex.internal.functions.Functions
import io.reactivex.subjects.PublishSubject
import org.adridadou.ethereum.propeller.backend.{EthereumTest, TestConfig}
import org.adridadou.ethereum.propeller.keystore.AccountProvider
import org.adridadou.ethereum.propeller.values.EthValue._
import org.adridadou.ethereum.propeller.values.{CallDetails, _}
import org.scalatest.check.Checkers
import org.scalatest.{FlatSpec, Matchers}
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.compat.java8.FutureConverters
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success}

/**
  * Created by felipe.forbeck on 21.02.19.
  * This code is released under Apache 2 license
  */
class BatchTransactionTest extends FlatSpec with Matchers with Checkers {

  private val logger = LoggerFactory.getLogger(classOf[BatchTransactionTest])

  private implicit val ec = ExecutionContext.global

  private val mainAccount = AccountProvider.fromSeed("Main Test Account")
  private val ethereum = CoreEthereumFacadeProvider
    .create(new EthereumTest(TestConfig.builder.balance(mainAccount, ether(1000)).build), EthereumConfig.builder().build())

  private val pendingTransactions = mutable.Map[Int, Future[CallDetails]]()
  private val publisher: PublishSubject[Int] = PublishSubject.create()


  it should "send all the transactions to the ethereum network and complete with success" in {

    submitTransactions(1, 25)
    submitTransactions(26, 50)
    submitTransactions(51, 75)
    submitTransactions(76, 100)

    Observable.empty()
        .mergeWith(publisher)
        .toFlowable(BackpressureStrategy.BUFFER)
        .doOnError(err => {
          logger.error(s"Transaction failed: ${err.getMessage}", err)
          pendingTransactions.clear()
          publisher.onComplete()
        })
        .doOnNext(txId => {
          logger.info(s"Transaction processed: ${txId}")
          pendingTransactions.remove(txId)
        })
        .doOnComplete(() => {
          logger.info(s"Pending transactions: ${pendingTransactions.size}")
          publisher.onTerminateDetach()
        })
        .timeout(15, TimeUnit.SECONDS)
        .onTerminateDetach()
        .blockingSubscribe()
  }

  private def submitTransactions(initialId: Int, endId: Int): Future[Unit] = {
    Future {
      (initialId to endId).foreach { id =>
        pendingTransactions += (id -> submitTransaction(id))
      }
    }
  }

  private def submitTransaction(id: Int): Future[CallDetails] = {
    val targetAccount = AccountProvider.fromSeed(id.toString)
    val data = EthData.of(s"Test: Sending Transaction ${id}".getBytes())
    val eventualResult = ethereum.sendTx(ether(0), data, mainAccount, targetAccount.getAddress)
    val timedResult = timedFuture[CallDetails](id, eventualResult)
    timedResult
  }

  private def timedFuture[T](id: Int, future: CompletableFuture[T]): Future[T] = {
    val eventualDetails = FutureConverters.toScala(future)
    val start = System.currentTimeMillis()
    eventualDetails.onComplete {
      case Failure(err) =>
        publisher.onError(new Exception(s"${System.currentTimeMillis() - start} ms. Error while waiting for call details of transaction ${id}, error: ${err.getMessage}", err))
      case Success(result) => result match {
        case details: CallDetails => {
          details.getTxHash should not be null
          logger.debug(s"Transaction $id took ${System.currentTimeMillis() - start} ms, txHash: ${details.getTxHash.withLeading0x()}")
          timedReceiptFuture(id, details.getResult)
        }
      }
    }
    eventualDetails
  }

  private def timedReceiptFuture[T](id: Int, future: CompletableFuture[T]): Future[T] = {
    val eventualReceipt = FutureConverters.toScala(future)
    val start = System.currentTimeMillis()
    eventualReceipt.onComplete {
      case Failure(err) =>
        publisher.onError(new Exception(s"Error while waiting for receipt of transaction ${id}, error: ${err.getMessage}", err))
      case Success(result) => result match {
        case receipt: TransactionReceipt => {
          receipt.isSuccessful shouldBe true
          logger.debug(s"Transaction Receipt $id took ${System.currentTimeMillis() - start} ms, isSuccessful: ${receipt.isSuccessful}")
          publisher.onNext(id)
          if (pendingTransactions.isEmpty) {
            publisher.onComplete()
          }
        }
      }
    }
    eventualReceipt
  }

}

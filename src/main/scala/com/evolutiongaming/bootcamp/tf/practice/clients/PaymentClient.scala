package com.evolutiongaming.bootcamp.tf.practice.clients

import com.evolutiongaming.bootcamp.tf.practice.domain.payment.{Payment, PaymentId}

import scala.concurrent.{ExecutionContextExecutor, Future}

trait PaymentClient {
  def process(token: String, payment: Payment): Future[PaymentId]
}

object PaymentClient {

  private implicit val executionContext: ExecutionContextExecutor =
    scala.concurrent.ExecutionContext.global

  def apply(): PaymentClient =
    new PaymentClient {
      def process(token: String, payment: Payment): Future[PaymentId] =
        Future { PaymentId("paymentId") }
    }
}

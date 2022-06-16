package com.evolutiongaming.bootcamp.tf.practice.services

import com.evolutiongaming.bootcamp.tf.practice.clients.PaymentClient
import com.evolutiongaming.bootcamp.tf.practice.domain.payment._
import com.evolutiongaming.bootcamp.tf.practice.effects.{FromFuture, ToFuture}

import scala.concurrent.Future

trait PaymentService[F[_]] {
  def process(payment: Payment): F[PaymentId]
}

object PaymentService {

  private val token = "token"

  def apply[F[_]: FromFuture](
    paymentClient: PaymentClient
  ): PaymentService[F] =
    (payment: Payment) =>
      FromFuture[F].apply {
        paymentClient.process(token, payment)
      }

  implicit class PaymentServiceOps[F[_]](self: PaymentService[F]) {

    def toUnsafe(implicit toFuture: ToFuture[F]): PaymentService[Future] =
      new PaymentService[Future] {
        def process(payment: Payment): Future[PaymentId] =
          toFuture(self.process(payment))
      }
  }
}

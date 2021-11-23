package com.evolutiongaming.bootcamp.tf.practice.services

import cats.MonadThrow
import cats.syntax.all._
import com.evolutiongaming.bootcamp.tf.practice.clients.FileClient
import com.evolutiongaming.bootcamp.tf.practice.domain._
import com.evolutiongaming.bootcamp.tf.practice.domain.cart.CartItem
import com.evolutiongaming.bootcamp.tf.practice.domain.order._
import com.evolutiongaming.bootcamp.tf.practice.domain.payment.PaymentId
import com.evolutiongaming.bootcamp.tf.practice.effects.UUIDSupport

import scala.util.control.NoStackTrace

trait OrderService[F[_]] {

  def create(
    userId: UserId,
    paymentId: PaymentId,
    items: List[CartItem],
    total: Money
  ): F[OrderId]

  def find(orderId: OrderId): F[Option[Order]]

  def findOrThrow(orderId: OrderId): F[Order]
}

object OrderService {

  private val fileName = "src/main/resources/orders"

  sealed trait OrderServiceError extends RuntimeException with NoStackTrace
  object OrderServiceError {
    final case class OrderNotFound(orderId: OrderId) extends OrderServiceError
  }

  def apply[F[_]: MonadThrow: UUIDSupport](fileClient: FileClient[F]): OrderService[F] =
    new OrderService[F] {

      def create(
        userId: UserId,
        paymentId: PaymentId,
        items: List[CartItem],
        total: Money
      ): F[OrderId] =
        for {
          orderId <- UUIDSupport[F].random.map(uuid => OrderId(uuid))
          order    = Order(orderId, userId, paymentId, items, total)
          orders  <- fileClient.read[List[Order]](fileName)
          _       <- fileClient.write(fileName, orders :+ order)
        } yield orderId

      def find(orderId: OrderId): F[Option[Order]] =
        fileClient
          .read[List[Order]](fileName)
          .map(_.find(_.id == orderId))

      def findOrThrow(orderId: OrderId): F[Order] =
        fileClient
          .read[List[Order]](fileName)
          .flatMap { orders =>
            orders
              .find(_.id == orderId)
              .map(_.pure[F])
              .getOrElse(MonadThrow[F].raiseError(OrderServiceError.OrderNotFound(orderId)))
          }
    }
}

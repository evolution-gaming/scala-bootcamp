package com.evolutiongaming.bootcamp.tf.practice.services

import cats.{Monad, MonadThrow}
import cats.syntax.all._
import com.evolutiongaming.bootcamp.tf.practice.clients.FileClient
import com.evolutiongaming.bootcamp.tf.practice.domain._
import com.evolutiongaming.bootcamp.tf.practice.domain.cart.CartItem
import com.evolutiongaming.bootcamp.tf.practice.domain.order._
import com.evolutiongaming.bootcamp.tf.practice.domain.payment.PaymentId
import com.evolutiongaming.bootcamp.tf.practice.effects.UUIDSupport

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

  def apply[F[_]: MonadThrow: UUIDSupport](
    fileClient: FileClient[F]
  ): OrderService[F] =
    new OrderService[F] {

      def create(
        userId: UserId,
        paymentId: PaymentId,
        items: List[CartItem],
        total: Money
      ): F[OrderId] =
        for {
          orderId <- UUIDSupport[F].random.map(id => OrderId(id))
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
          .map(_.find(_.id == orderId))
          .flatMap { orderOpt =>
            orderOpt
              .map(_.pure[F])
              .getOrElse(MonadThrow[F].raiseError(new RuntimeException("order not found")))
          }
    }
}

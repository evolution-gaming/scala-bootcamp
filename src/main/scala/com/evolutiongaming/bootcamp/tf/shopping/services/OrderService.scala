package com.evolutiongaming.bootcamp.tf.shopping.services

import cats.Monad
import cats.syntax.all._
import com.evolutiongaming.bootcamp.tf.shopping.clients.FileClient
import com.evolutiongaming.bootcamp.tf.shopping.domain.cart.CartItem
import com.evolutiongaming.bootcamp.tf.shopping.domain.money.Money
import com.evolutiongaming.bootcamp.tf.shopping.domain.order.{Order, OrderId}
import com.evolutiongaming.bootcamp.tf.shopping.domain.payment.PaymentId
import com.evolutiongaming.bootcamp.tf.shopping.domain.user.UserId

import java.util.UUID

trait OrderService[F[_]] {

  def create(
    userId: UserId,
    paymentId: PaymentId,
    items: List[CartItem],
    total: Money
  ): F[OrderId]

  def find(orderId: OrderId): F[Option[Order]]

}

object OrderService {

  private val fileName = "src/main/resources/orders"

  def apply[F[_]: Monad](fileClient: FileClient[F]): OrderService[F] = new OrderService[F] {
    override def create(userId: UserId, paymentId: PaymentId, items: List[CartItem], total: Money): F[OrderId] = {
      val newId = OrderId(UUID.randomUUID())
      val order = Order(newId, userId, paymentId, items, total)

      for {
        orders <- fileClient.read[List[Order]](fileName)
        _      <- fileClient.write(fileName, orders :+ order)
      } yield newId
    }

    override def find(orderId: OrderId): F[Option[Order]] =
      for {
        orders <- fileClient.read[List[Order]](fileName)
      } yield orders.find(_.id == orderId)
  }

}

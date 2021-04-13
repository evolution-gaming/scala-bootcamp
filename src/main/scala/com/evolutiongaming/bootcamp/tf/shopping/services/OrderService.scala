package com.evolutiongaming.bootcamp.tf.shopping.services

import com.evolutiongaming.bootcamp.tf.shopping.domain.cart.CartItem
import com.evolutiongaming.bootcamp.tf.shopping.domain.money.Money
import com.evolutiongaming.bootcamp.tf.shopping.domain.order.{Order, OrderId}
import com.evolutiongaming.bootcamp.tf.shopping.domain.payment.PaymentId
import com.evolutiongaming.bootcamp.tf.shopping.domain.user.UserId

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

}

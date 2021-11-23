package com.evolutiongaming.bootcamp.tf.practice.services

import com.evolutiongaming.bootcamp.tf.practice.domain._
import com.evolutiongaming.bootcamp.tf.practice.domain.cart.CartItem
import com.evolutiongaming.bootcamp.tf.practice.domain.order._
import com.evolutiongaming.bootcamp.tf.practice.domain.payment.PaymentId

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

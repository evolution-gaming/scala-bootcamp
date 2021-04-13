package com.evolutiongaming.bootcamp.tf.shopping.domain

import com.evolutiongaming.bootcamp.tf.shopping.domain.cart.CartItem
import com.evolutiongaming.bootcamp.tf.shopping.domain.money.Money
import com.evolutiongaming.bootcamp.tf.shopping.domain.payment.PaymentId
import com.evolutiongaming.bootcamp.tf.shopping.domain.user.UserId

import java.util.UUID

object order {

  final case class OrderId(value: UUID)
  final case class Order(id: OrderId, userId: UserId, paymentId: PaymentId, items: List[CartItem], total: Money)

}

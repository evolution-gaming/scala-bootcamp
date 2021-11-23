package com.evolutiongaming.bootcamp.tf.practice.domain.order

import com.evolutiongaming.bootcamp.tf.practice.domain.{Money, UserId}
import com.evolutiongaming.bootcamp.tf.practice.domain.cart.CartItem
import com.evolutiongaming.bootcamp.tf.practice.domain.payment.PaymentId
import io.circe.generic.JsonCodec

@JsonCodec
final case class Order(
  id: OrderId,
  userId: UserId,
  paymentId: PaymentId,
  items: List[CartItem],
  total: Money
)

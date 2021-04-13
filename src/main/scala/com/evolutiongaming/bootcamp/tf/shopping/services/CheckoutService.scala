package com.evolutiongaming.bootcamp.tf.shopping.services

import com.evolutiongaming.bootcamp.tf.shopping.domain.card.Card
import com.evolutiongaming.bootcamp.tf.shopping.domain.order.OrderId
import com.evolutiongaming.bootcamp.tf.shopping.domain.user.UserId

sealed trait CheckoutError

/*
During checkout process we need to:
- find cart
- create payment
- create order
- delete cart
 */

trait CheckoutService[F[_]] {
  def checkout(userId: UserId, card: Card): F[Either[CheckoutError, OrderId]]
}

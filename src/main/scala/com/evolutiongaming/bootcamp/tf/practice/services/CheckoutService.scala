package com.evolutiongaming.bootcamp.tf.practice.services

import com.evolutiongaming.bootcamp.tf.practice.domain.UserId
import com.evolutiongaming.bootcamp.tf.practice.domain.order.OrderId
import com.evolutiongaming.bootcamp.tf.practice.domain.payment.Card
import com.evolutiongaming.bootcamp.tf.practice.services.CheckoutService.CheckoutError

trait CheckoutService[F[_]] {
  def checkout(userId: UserId, card: Card): F[Either[CheckoutError, OrderId]]
}

object CheckoutService {

  sealed trait CheckoutError
}
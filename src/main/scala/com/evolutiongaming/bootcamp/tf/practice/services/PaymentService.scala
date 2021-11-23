package com.evolutiongaming.bootcamp.tf.practice.services

import com.evolutiongaming.bootcamp.tf.practice.domain.payment._

trait PaymentService[F[_]] {
  def process(payment: Payment): F[PaymentId]
}

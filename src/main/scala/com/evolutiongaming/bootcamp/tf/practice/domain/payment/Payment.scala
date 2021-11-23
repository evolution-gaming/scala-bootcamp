package com.evolutiongaming.bootcamp.tf.practice.domain.payment

import com.evolutiongaming.bootcamp.tf.practice.domain.{Money, UserId}

final case class Payment(userId: UserId, amount: Money, card: Card)

package com.evolutiongaming.bootcamp.tf.practice.domain.cart

import com.evolutiongaming.bootcamp.tf.practice.domain.UserId

import java.util.Currency

final case class Cart(
  userId: UserId,
  items: List[CartItem],
  currency: Currency
)

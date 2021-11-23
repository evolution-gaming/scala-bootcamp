package com.evolutiongaming.bootcamp.tf.practice.domain.cart

import com.evolutiongaming.bootcamp.tf.practice.domain.Money

final case class CartTotal(items: List[CartItem], total: Money)

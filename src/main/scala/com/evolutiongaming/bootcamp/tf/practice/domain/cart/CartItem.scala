package com.evolutiongaming.bootcamp.tf.practice.domain.cart

final case class CartItem(
  itemId: ItemId,
  quantity: Quantity,
  price: BigDecimal
)

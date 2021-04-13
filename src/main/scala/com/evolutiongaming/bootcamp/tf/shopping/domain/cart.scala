package com.evolutiongaming.bootcamp.tf.shopping.domain

import com.evolutiongaming.bootcamp.tf.shopping.domain.item.ItemId
import com.evolutiongaming.bootcamp.tf.shopping.domain.money.Money
import com.evolutiongaming.bootcamp.tf.shopping.domain.user.UserId

import java.util.Currency

object cart {

  final case class Quantity(amount: Long)
  final case class CartTotal(items: List[CartItem], total: Money)
  final case class CartItem(itemId: ItemId, quantity: Quantity, price: BigDecimal)
  final case class Cart(userId: UserId, items: List[CartItem], currency: Currency)

}

package com.evolutiongaming.bootcamp.tf.shopping.services

import com.evolutiongaming.bootcamp.tf.shopping.domain.cart._
import com.evolutiongaming.bootcamp.tf.shopping.domain.item._
import com.evolutiongaming.bootcamp.tf.shopping.domain.money.Money
import com.evolutiongaming.bootcamp.tf.shopping.domain.user._

sealed trait CartError

trait ShoppingCartService[F[_]] {
  def add(userId: UserId, itemId: ItemId, quantity: Quantity, price: Money): F[Either[CartError, Unit]]
  def get(userId: UserId): F[Option[CartTotal]]
  def delete(userId: UserId): F[Unit]
}

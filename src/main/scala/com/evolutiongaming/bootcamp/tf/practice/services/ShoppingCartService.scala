package com.evolutiongaming.bootcamp.tf.practice.services

import com.evolutiongaming.bootcamp.tf.practice.domain._
import com.evolutiongaming.bootcamp.tf.practice.domain.cart._

sealed trait CartError

trait ShoppingCartService[F[_]] {

  def add(
    userId: UserId,
    itemId: ItemId,
    quantity: Quantity,
    price: Money
  ): F[Either[CartError, Unit]]

  def get(userId: UserId): F[Option[CartTotal]]

  def delete(userId: UserId): F[Unit]
}

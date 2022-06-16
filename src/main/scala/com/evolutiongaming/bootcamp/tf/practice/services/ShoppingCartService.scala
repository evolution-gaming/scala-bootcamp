package com.evolutiongaming.bootcamp.tf.practice.services

import cats.Functor
import cats.effect.Sync
import cats.syntax.all._
import cats.effect.concurrent.Ref
import com.evolutiongaming.bootcamp.tf.practice.domain._
import com.evolutiongaming.bootcamp.tf.practice.domain.cart._
import com.evolutiongaming.bootcamp.tf.practice.services.ShoppingCartService.CartError

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

object ShoppingCartService {

  sealed trait CartError
  object CartError {
    case object CurrencyMismatch extends CartError
  }

  def of[F[_]: Sync]: F[ShoppingCartService[F]] =
    Ref.of(Map.empty[UserId, Cart]).map(apply(_))

  def apply[F[_]: Functor](
    state: Ref[F, Map[UserId, Cart]] // Ref.of -> F[Ref[F, Map[K, V]]] -> F[ShoppingCartService[F]]
  ): ShoppingCartService[F] =
    new ShoppingCartService[F] {

      def add(
        userId: UserId,
        itemId: ItemId,
        quantity: Quantity,
        price: Money
      ): F[Either[CartError, Unit]] =
        state.modify { carts =>
          val cart = carts.getOrElse(userId, Cart(userId, List.empty, price.currency))

          if (price.currency != cart.currency)
            (carts, CartError.CurrencyMismatch.asLeft)
          else {
            val updatedCart = cart.copy(items = cart.items :+ CartItem(itemId, quantity, price.amount))
            (carts + (userId -> updatedCart), ().asRight)
          }
        }

      def get(userId: UserId): F[Option[CartTotal]] =
        state.get.map { carts =>
          carts.get(userId).map { cart =>
            CartTotal(
              cart.items,
              Money(
                cart.items.map(item => item.quantity.amount * item.price).sum,
                cart.currency
              )
            )
          }
        }

      def delete(userId: UserId): F[Unit] =
        state.update(_.removed(userId))
    }
}

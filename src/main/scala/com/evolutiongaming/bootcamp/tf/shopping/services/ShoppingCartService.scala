package com.evolutiongaming.bootcamp.tf.shopping.services

import cats.data.State
import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.syntax.all._
import com.evolutiongaming.bootcamp.tf.shopping.domain.cart._
import com.evolutiongaming.bootcamp.tf.shopping.domain.item._
import com.evolutiongaming.bootcamp.tf.shopping.domain.money.Money
import com.evolutiongaming.bootcamp.tf.shopping.domain.user._
import com.evolutiongaming.bootcamp.tf.shopping.services.CartError.CurrencyMismatch

sealed trait CartError

object CartError {
  case object CurrencyMismatch extends CartError
}

trait ShoppingCartService[F[_]] {
  def add(userId: UserId, itemId: ItemId, quantity: Quantity, price: Money): F[Either[CartError, Unit]]
  def get(userId: UserId): F[Option[CartTotal]]
  def delete(userId: UserId): F[Unit]
}

object ShoppingCartService {

  def of[F[_]: Sync]: F[ShoppingCartService[F]] =
    Ref.of(Map.empty[UserId, Cart]).map { state =>
      new ShoppingCartService[F] {
        override def add(userId: UserId, itemId: ItemId, quantity: Quantity, price: Money): F[Either[CartError, Unit]] =
          state.modifyState {
            State { carts =>
              val cart = carts.getOrElse(userId, Cart(userId, Nil, price.currency))

              if (price.currency != cart.currency)
                (carts, CurrencyMismatch.asLeft)
              else {
                val updatedCart = cart.copy(items = cart.items :+ CartItem(itemId, quantity, price.amount))
                (carts + (userId -> updatedCart), ().asRight)
              }
            }
          }

        override def get(userId: UserId): F[Option[CartTotal]] =
          state.get.map { carts =>
            carts.get(userId).map { cart =>
              CartTotal(cart.items, Money(cart.items.map(_.price).combineAll, cart.currency))
            }
          }

        override def delete(userId: UserId): F[Unit] =
          state.update(_.removed(userId))
      }
    }

}

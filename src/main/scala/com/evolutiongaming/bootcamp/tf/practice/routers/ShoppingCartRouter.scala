package com.evolutiongaming.bootcamp.tf.practice.routers

import cats.Monad
import cats.syntax.all._
import cats.data.{Kleisli, OptionT}
import com.evolutiongaming.bootcamp.tf.practice.domain.{Money, UserId}
import com.evolutiongaming.bootcamp.tf.practice.domain.cart.{ItemId, Quantity}
import com.evolutiongaming.bootcamp.tf.practice.effects.{CurrencySupport, ToNumeric, UUIDSupport}
import com.evolutiongaming.bootcamp.tf.practice.effects.ToNumeric.toNumericStringOps
import com.evolutiongaming.bootcamp.tf.practice.effects.CurrencySupport.CurrencySupportStringOps
import com.evolutiongaming.bootcamp.tf.practice.services.ShoppingCartService

object ShoppingCartRouter {

  def apply[F[_]: Monad: UUIDSupport: ToNumeric: CurrencySupport](
    shoppingCartService: ShoppingCartService[F]
  ): Kleisli[OptionT[F, *], List[String], String] =
    Kleisli[OptionT[F, *], List[String], String] {
      case "get" :: userId :: Nil                                            =>
        OptionT.liftF {
          for {
            userId <- UUIDSupport[F].read(userId)
            result <- shoppingCartService.get(UserId(userId))
          } yield result.toString
        }

      case "add" :: userId :: itemId :: quantity :: price :: currency :: Nil =>
        OptionT.liftF {
          for {
            userId   <- UUIDSupport[F].read(userId)
            itemId   <- UUIDSupport[F].read(itemId)
            quantity <- quantity.toLongF
            price    <- price.toBigDecimalF
            currency <- currency.toCurrency
            result   <- shoppingCartService.add(
                          UserId(userId),
                          ItemId(itemId),
                          Quantity(quantity),
                          Money(price, currency)
                        )
          } yield result.toString
        }

      case _                                                                 =>
        OptionT.none
    }
}

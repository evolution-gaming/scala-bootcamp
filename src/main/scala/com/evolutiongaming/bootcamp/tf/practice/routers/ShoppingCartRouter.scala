package com.evolutiongaming.bootcamp.tf.practice.routers

import cats.Monad
import cats.syntax.all._
import cats.data.{Kleisli, OptionT}
import com.evolutiongaming.bootcamp.tf.practice.domain.{Money, UserId}
import com.evolutiongaming.bootcamp.tf.practice.domain.cart.{ItemId, Quantity}
import com.evolutiongaming.bootcamp.tf.practice.effects.{CurrencySupport, ToNumeric, UUIDSupport}
import com.evolutiongaming.bootcamp.tf.practice.effects.ToNumeric.ToNumericStringOps
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
            userId <- UUIDSupport[F].read(userId).map(id => UserId(id))
            result <- shoppingCartService.get(userId)
          } yield result.toString
        }

      case "add" :: userId :: itemId :: quantity :: price :: currency :: Nil =>
        OptionT.liftF {
          for {
            userId   <- UUIDSupport[F].read(userId).map(id => UserId(id))
            itemId   <- UUIDSupport[F].read(itemId).map(id => ItemId(id))
            quantity <- quantity.toLongF.map(q => Quantity(q))
            price    <- price.toBigDecimalF
            currency <- currency.toCurrency
            result   <- shoppingCartService.add(
                          userId = userId,
                          itemId = itemId,
                          quantity = quantity,
                          price = Money(price, currency)
                        )
          } yield result.toString
        }

      case _                                                                 =>
        OptionT.none[F, String]
    }
}

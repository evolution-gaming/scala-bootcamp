package com.evolutiongaming.bootcamp.tf.practice.routers

import cats.Monad
import cats.syntax.all._
import cats.data.{Kleisli, OptionT}
import com.evolutiongaming.bootcamp.tf.practice.domain.UserId
import com.evolutiongaming.bootcamp.tf.practice.domain.payment.Card
import com.evolutiongaming.bootcamp.tf.practice.effects.UUIDSupport
import com.evolutiongaming.bootcamp.tf.practice.services.CheckoutService

object CheckoutRouter {
  def apply[F[_]: Monad: UUIDSupport](
    checkoutService: CheckoutService[F]
  ): Kleisli[OptionT[F, *], List[String], String] =
    Kleisli[OptionT[F, *], List[String], String] {

      case userId :: cardNumber :: cvv :: Nil =>
        OptionT.liftF {
          for {
            userId <- UUIDSupport[F].read(userId).map(id => UserId(id))
            result <- checkoutService.checkout(userId, Card(cardNumber, cvv))
          } yield result.toString
        }

      case _                                  =>
        OptionT.none[F, String]
    }
}

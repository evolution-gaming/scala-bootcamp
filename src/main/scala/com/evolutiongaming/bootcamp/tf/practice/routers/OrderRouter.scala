package com.evolutiongaming.bootcamp.tf.practice.routers

import cats.Monad
import cats.data.{Kleisli, OptionT}
import cats.syntax.all._
import com.evolutiongaming.bootcamp.tf.practice.domain.order.OrderId
import com.evolutiongaming.bootcamp.tf.practice.effects.UUIDSupport
import com.evolutiongaming.bootcamp.tf.practice.services.OrderService

object OrderRouter {
  def apply[F[_]: Monad: UUIDSupport](
    orderService: OrderService[F]
  ): Kleisli[OptionT[F, *], List[String], String] =
    Kleisli[OptionT[F, *], List[String], String] { // A => F[B]

      case "find" :: orderId :: Nil =>
        OptionT.liftF {
          for {
            orderId <- UUIDSupport[F].read(orderId).map(id => OrderId(id))
            result  <- orderService.find(orderId)
          } yield result.toString
        }

      case _                        =>
        OptionT.none[F, String]
    }
}

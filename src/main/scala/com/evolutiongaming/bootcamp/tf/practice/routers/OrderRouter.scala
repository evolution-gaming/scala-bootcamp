package com.evolutiongaming.bootcamp.tf.practice.routers

import cats.Monad
import cats.syntax.all._
import cats.data.{Kleisli, OptionT}
import com.evolutiongaming.bootcamp.tf.practice.domain.order.OrderId
import com.evolutiongaming.bootcamp.tf.practice.effects.UUIDSupport
import com.evolutiongaming.bootcamp.tf.practice.services.OrderService

object OrderRouter {

  def apply[F[_]: Monad: UUIDSupport](
    orderService: OrderService[F]
  ): Kleisli[OptionT[F, *], List[String], String] =
    Kleisli[OptionT[F, *], List[String], String] {
      case "find" :: orderId :: Nil =>
        OptionT.liftF {
          for {
            orderId <- UUIDSupport[F].read(orderId)
            result  <- orderService.find(OrderId(orderId))
          } yield result.toString
        }

      case _                        => OptionT.none
    }
}

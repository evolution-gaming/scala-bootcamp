package com.evolutiongaming.bootcamp.tf.shopping.routers

import cats.Applicative
import cats.data.{Kleisli, OptionT}

object RootRouter {

  def apply[F[_]: Applicative](
    shoppingCartRouter: Kleisli[OptionT[F, *], List[String], String],
    checkoutRouter: Kleisli[OptionT[F, *], List[String], String],
    orderRouter: Kleisli[OptionT[F, *], List[String], String]
  ): Kleisli[OptionT[F, *], List[String], String] = ???

}

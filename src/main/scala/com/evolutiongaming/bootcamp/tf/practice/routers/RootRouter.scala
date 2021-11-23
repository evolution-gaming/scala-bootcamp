package com.evolutiongaming.bootcamp.tf.practice.routers

import cats.data.{Kleisli, OptionT}

object RootRouter {
  def apply[F[_]](
    shoppingCartRouter: Kleisli[OptionT[F, *], List[String], String],
    checkoutRouter: Kleisli[OptionT[F, *], List[String], String],
    orderRouter: Kleisli[OptionT[F, *], List[String], String]
  ): Kleisli[OptionT[F, *], List[String], String] = ???
}

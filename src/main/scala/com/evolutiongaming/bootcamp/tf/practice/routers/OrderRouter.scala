package com.evolutiongaming.bootcamp.tf.practice.routers

import cats.data.{Kleisli, OptionT}

object OrderRouter {
  def apply[F[_]]: Kleisli[OptionT[F, *], List[String], String] = ???
}

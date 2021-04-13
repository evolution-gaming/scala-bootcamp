package com.evolutiongaming.bootcamp.tf.shopping.effects

trait ToNumeric[F[_]] {
  def toLong(str: String): F[Long]
  def toBigDecimal(str: String): F[BigDecimal]
}

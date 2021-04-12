package com.evolutiongaming.bootcamp.tf.shopping.effects

import cats.effect.Sync

trait ToNumeric[F[_]] {
  def toLong(str: String): F[Long]
  def toBigDecimal(str: String): F[BigDecimal]
}

object ToNumeric {

  def apply[F[_]: ToNumeric]: ToNumeric[F] = implicitly

  implicit class ToNumericStringOps[F[_]: ToNumeric](str: String) {
    def toLongF: F[Long]             = ToNumeric[F].toLong(str)
    def toBigDecimalF: F[BigDecimal] = ToNumeric[F].toBigDecimal(str)
  }

  implicit def toNumeric[F[_]: Sync]: ToNumeric[F] = new ToNumeric[F] {
    override def toLong(str: String): F[Long]             = Sync[F].delay(str.toLong)
    override def toBigDecimal(str: String): F[BigDecimal] = Sync[F].delay(BigDecimal(str))
  }
}

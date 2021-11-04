package com.evolutiongaming.bootcamp.tf.v2

import com.evolutiongaming.bootcamp.tf.v2.Items.ValidationError.{EmptyName, NegativePrice}
import com.evolutiongaming.bootcamp.tf.v2.Items.{Item, ValidationError}

trait Items[F[_]] {
  def all: F[Map[Long, Item]]
  def create(name: String, price: BigDecimal): F[Either[ValidationError, Item]]
  def update(item: Item): F[Either[ValidationError, Boolean]]
  def find(id: Long): F[Option[Item]]
  def delete(id: Long): F[Boolean]
}

object Items {

  final case class Item(
    id: Long,
    name: String,
    price: BigDecimal
  )

  sealed trait ValidationError extends Throwable

  object ValidationError {
    case object EmptyName     extends ValidationError
    case object NegativePrice extends ValidationError
  }

  private[tf] def validate(name: String, price: BigDecimal): Either[ValidationError, (String, BigDecimal)] =
    for {
      name  <- Either.cond(name.nonEmpty, name, EmptyName)
      price <- Either.cond(price > 0, price, NegativePrice)
    } yield (name, price)
}

trait Console[F[_]] {
  def readStr: F[String]
  def readBigDecimal: F[BigDecimal]
  def putStrLn(str: String): F[Unit]
}

object Warehouse extends App {}

package com.evolutiongaming.bootcamp.tf

import cats._
import cats.effect._
import cats.effect.concurrent.Ref
import cats.syntax.all._
import com.evolutiongaming.bootcamp.tf.Items.ValidationError
import com.evolutiongaming.bootcamp.tf.Items.ValidationError.{EmptyName, NegativePrice}

import scala.io.StdIn

final case class Item(id: Long, name: String, price: BigDecimal)

trait Items[F[_]] {
  def all: F[Map[Long, Item]]
  def create(name: String, price: BigDecimal): F[Either[ValidationError, Item]]
  def update(item: Item): F[Either[ValidationError, Boolean]]
  def find(id: Long): F[Option[Item]]
  def delete(id: Long): F[Boolean]
}

object Items {

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

  def of[F[_]: Sync]: F[Items[F]] = for {
    counter <- Ref.of[F, Long](0)
    items   <- Ref.of[F, Map[Long, Item]](Map.empty)
  } yield new ItemsImpl(counter, items)
}

private final class ItemsImpl[F[_]: Monad](
  counter: Ref[F, Long],
  items: Ref[F, Map[Long, Item]]
) extends Items[F] {

  override def all: F[Map[Long, Item]] = items.get

  override def create(name: String, price: BigDecimal): F[Either[ValidationError, Item]] =
    Items.validate(name, price).traverse { case (name, price) =>
      for {
        id   <- counter.updateAndGet(_ + 1)
        item =  Item(id, name, price)
        _    <- items.update(_.updated(id, item))
      } yield item
    }

  override def update(item: Item): F[Either[ValidationError, Boolean]] =
    Items.validate(item.name, item.price).traverse { _ =>
      items.modify { items =>
        if (items.contains(item.id)) items.updated(item.id, item) -> true
        else items -> false
      }
    }

  override def find(id: Long): F[Option[Item]] = items.get.map(_.get(id))

  override def delete(id: Long): F[Boolean] = items.modify { items =>
    items.removed(id) -> items.contains(id)
  }
}



trait Console[F[_]] {
  def readStr: F[String]
  def readBigDecimal: F[BigDecimal]
  def putStrLn(str: String): F[Unit]
}

object Console {

  def apply[F[_]: Sync]: Console[F] = new Console[F] {

    override def readStr: F[String] = Sync[F].delay(StdIn.readLine())

    override def readBigDecimal: F[BigDecimal] = readStr.flatMap { str =>
      try {
        BigDecimal(str).pure[F]
      } catch {
        case _: Throwable => readBigDecimal
      }
    }

    override def putStrLn(str: String): F[Unit] = Sync[F].delay(println(str))
  }
}

object Warehouse extends App {

  def program[F[_]: Monad](console: Console[F], items: Items[F]): F[Either[ValidationError, Item]] =
    for {
      name  <- console.readStr
      price <- console.readBigDecimal
      item  <- items.create(name, price)
    } yield item


  val test = for {
    items   <- Items.of[IO]
    console =  Console[IO]
    result  <- program(console, items)
    _       <- console.putStrLn(result.toString)
  } yield ()
}

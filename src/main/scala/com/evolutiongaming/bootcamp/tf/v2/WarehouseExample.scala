package com.evolutiongaming.bootcamp.tf.v2

import cats.{Functor, Monad}
import cats.effect.{IO, Sync}
import cats.effect.concurrent.Ref
import cats.syntax.all._
import com.evolutiongaming.bootcamp.tf.v2.Items.ValidationError.{EmptyName, NegativePrice}
import com.evolutiongaming.bootcamp.tf.v2.Items.{Item, ValidationError}

import scala.io.StdIn

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

//  def apply[F[_]]: Interpreter
//  def of[F[_]]: F[Interpreter]
//  def make[F[_]]: Resource[F, Interpreter]

  def of[F[_]: Sync]: F[Items[F]] =
    for {
      counter <- Ref.of[F, Long](0L)
      items   <- Ref.of[F, Map[Long, Item]](Map.empty)
    } yield new ItemsImpl(counter, items)

  private final class ItemsImpl[F[_]: Monad](
    counter: Ref[F, Long],
    items: Ref[F, Map[Long, Item]]
  ) extends Items[F] {

    def all: F[Map[Long, Item]] = items.get

    def create(name: String, price: BigDecimal): F[Either[ValidationError, Item]] =
      Items.validate(name, price).traverse {
        case (name, price) =>
          for {
            id  <- counter.updateAndGet(_ + 1)
            item = Item(id, name, price)
            _   <- items.update(_.updated(id, item))
          } yield item
      }

    def update(item: Item): F[Either[ValidationError, Boolean]] =
      Items.validate(item.name, item.price).traverse { _ =>
        items.modify { items =>
          if (items.contains(item.id)) items.updated(item.id, item) -> true
          else items                                                -> false
        }
      }

    def find(id: Long): F[Option[Item]] = items.get.map(_.get(id))

    def delete(id: Long): F[Boolean] =
      items.modify { items =>
        (items.removed(id), items.contains(id))
      }
  }
}

trait Console[F[_]] {
  def readStr: F[String]
  def readBigDecimal: F[BigDecimal]
  def putStrLn(str: String): F[Unit]
}

object Console {

  def apply[F[_]: Sync]: Console[F] =
    new Console[F] {

      def readStr: F[String] = Sync[F].delay(StdIn.readLine())

      def readBigDecimal: F[BigDecimal] =
        readStr.flatMap { str =>
          try BigDecimal(str).pure[F]
          catch {
            case _: Throwable => putStrLn(s"$str is not a BigDecimal") *> readBigDecimal
//            case _: Throwable =>
//              putStrLn(s"$str is not a BigDecimal")
//                .flatMap(_ => readBigDecimal)
          }
        }

      def putStrLn(str: String): F[Unit] = Sync[F].delay(println(str))
    }
}

object Warehouse extends App {

  def program[F[_]: Monad](console: Console[F], items: Items[F]): F[Either[ValidationError, Item]] =
    for {
      name  <- console.readStr
      price <- console.readBigDecimal
      item  <- items.create(name, price)
    } yield item

  def iteration[F[_]: Monad](console: Console[F], items: Items[F]): F[Unit] =
    for {
      result <- program(console, items)
      _      <- console.putStrLn(result.toString)
      _      <- iteration(console, items)
    } yield ()

  val test: IO[Unit] =
    for {
      items  <- Items.of[IO]
      console = Console[IO]
      _      <- iteration(console, items)
    } yield ()

  test.unsafeRunSync()
}

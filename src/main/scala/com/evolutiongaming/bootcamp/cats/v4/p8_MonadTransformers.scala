package com.evolutiongaming.bootcamp.cats.v4

import cats.data.{EitherT, OptionT}
import cats.effect.IO
import cats.syntax.all._
import cats.{Functor, Monad}

object p8_MonadTransformers {

  /** The final topic for today is monad transformers.
    * So far we've seen a nice way to chain our executions via .map and .flatMap
    * But what if we need to stack different monads together?
    */

  // Different monads do not compose, unlike functors/applicatives.
  // For two functors F, G, it is possible to define functor for F[G[_]]
  def composedFunctor[F[_]: Functor, G[_]: Functor]: Functor[Lambda[T => F[G[T]]]] = {
    new Functor[Lambda[T => F[G[T]]]] {
      override def map[A, B](fga: F[G[A]])(f: A => B): F[G[B]] = fga.map(fa => fa.map(f))
    }
  }
  // Same is true for Applicative

  // This is not the case for monads
  def impossibleFlatMap[F[_]: Monad, G[_]: Monad, A, B](fa: F[G[A]])(f: A => F[G[B]]): F[G[B]] = ???

  // BUT, for some inner types (G), it's possible to have a monad for arbitrary outer type (F)
  // E.g. there are monads for F[Option[_]], F[Either[A, B]]
  // OptionT[F[_], A] is a wrapper around F[Option[A]]
  // EitherT[F[_], A, B] is a wrapper around F[Either[A, B]]

  // Monad transformers takes monad as an argument and returns transformed monad as a result

  // Commonly used with F = IO and the like

  // OptionT can be constructed in a variety of ways
  val ioOptInt: IO[Option[Int]] = IO(1.some)
  OptionT(ioOptInt) // From IO[Option[Int]]
  OptionT.liftF(IO(1)) // From IO[Int]
  OptionT.fromOption[IO](1.some) // From Option[Int]
  OptionT.pure[IO](1) // From Int

  // Same example as with Monad, but wrapped into IO
  trait User
  trait Gift
  trait GiftCard

  def fetchUser(): IO[Option[User]]                   = IO(???)
  def fetchGiftCard(user: User): IO[Option[GiftCard]] = IO(???)
  // Let's assume GiftCard always has a gift
  def pickGift(giftCard: GiftCard): IO[Gift]          = IO(???)

  // Let's try getting a gift without transformers
  def gift(): IO[Option[Gift]] = {
    for {
      userOpt     <- fetchUser()
      giftCardOpt <- userOpt match {
        case Some(user) => fetchGiftCard(user)
        case None       => none[GiftCard].pure[IO]
      }
      gift        <- giftCardOpt match {
        case Some(giftCard) => pickGift(giftCard).map(_.some)
        case None           => none[Gift].pure[IO]
      }
    } yield gift
  }

  // With OptionT
  def giftT(): OptionT[IO, Gift] = {
    for {
      user     <- OptionT(fetchUser())
      giftCard <- OptionT(fetchGiftCard(user))
      gift     <- OptionT.liftF(pickGift(giftCard))
    } yield gift
  }

  // Note that giftT returns OptionT
  // We can get back to underlying type with .value
  val alsoGift: IO[Option[Gift]] = giftT().value

  // Which method to use?
  trait UserRepository[F[_]] {
    def fetchUser(): F[Option[User]]

    def fetchUserT(): OptionT[F, User]
  }

  // EitherT
  val ioEitherInt: IO[Either[String, Int]] = IO(Right(1))
  EitherT(ioEitherInt) // From IO[Either[String, Int]]
  EitherT.liftF(IO(1)) // From IO[Int]
  EitherT.fromEither[IO](1.asRight[String]) // From Either[String, Int]
  EitherT.pure[IO, String](3) // From Int
  EitherT.fromOption[IO](1.some, "Error") // From Option[Int]
  EitherT.fromOptionF(IO(1.some), "Error") // From F[Option[Int]]

  /** Drawbacks:
    *  Each operation with monad transformers allocates an instance of a wrapper.
    *  Usually that's fine, but that can get noticeable in hot paths.
    */
}

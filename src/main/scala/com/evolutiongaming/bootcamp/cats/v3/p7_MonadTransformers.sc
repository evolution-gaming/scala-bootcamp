import cats.effect.IO
import cats.syntax.all._
import cats.{Functor, Monad}
// Monad Transformers

// Theory

// Monads do not compose, unlike functors/applicatives
// For two functors F, G, it is possible to define functor for F[G[_]]
def composedFunctor[F[_]: Functor, G[_]: Functor]: Functor[Lambda[T => F[G[T]]]]             = {
  new Functor[Lambda[T => F[G[T]]]] {
    override def map[A, B](fga: F[G[A]])(f: A => B): F[G[B]] = fga.map(fa => fa.map(f))
  }
}
// Same is true for Applicative
// This is not the case for monads
def impossibleFlatMap[F[_]: Monad, G[_]: Monad, A, B](fa: F[G[A]])(f: A => F[G[B]]): F[G[B]] = ???

// BUT, for some inner types (G), it's possible to have a monad for arbitrary outer type
// E.g. there is a monad for F[Option[_]]
// That's monad transformers

// Practice

// OptionT[F[_], A] is a wrapper around F[Option[A]]
// EitherT[F[_], A, B] is a wrapper around F[Either[A, B]]
// Has a monad (and usable in for-comprehensions) iff F has a monad
// Commonly used with F = IO and the like

import cats.data.OptionT

// OptionT can be constructed in a variety of ways
val ioOptInt = IO(Option(1))
OptionT(ioOptInt) // From IO[Option[Int]]
OptionT.liftF(IO(1)) // From IO[Int]
OptionT.fromOption[IO](Some(1)) // From Option[Int]
OptionT.pure[IO](1) // From Int

// Same example as with Monad, but with IO[Option[_]]
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

// Drawbacks:
// Each operation with monad transformers allocates an instance of a wrapper.
// Usually that's fine, but that can get noticeable in hot paths.

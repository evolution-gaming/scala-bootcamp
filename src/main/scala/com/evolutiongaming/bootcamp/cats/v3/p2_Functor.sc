// Functor
import cats.Functor

// Functor is a "map" operation as a typeclass
// Note that it's type parameter is of kind * -> *
// Here's an instance for Option
object OptionFunctor extends Functor[Option] {
  override def map[A, B](fa: Option[A])(f: A => B) = fa match {
    case Some(value) => Some(f(value))
    case None        => None
  }
}

// Main laws: fa.map(identity) <-> fa, fa.map(f).map(g) <-> fa.map(a => g(f(a))
// Laws keep depending operations from breaking, e.g. this compiles, but unlawful
object IllegalOptionFunctor extends Functor[Option] {
  override def map[A, B](fa: Option[A])(f: A => B) = None
}
// There's cats-laws module for scalacheck-based law verification
// https://github.com/typelevel/cats/blob/main/laws/src/main/scala/cats/laws/FunctorLaws.scala

// We have an abstraction for map, let's try writing code which works for any wrapper type
import cats.syntax.functor._

case class User(name: String, age: Int)

def getUserAge[F[_]: Functor](user: F[User]): F[Int] = user.map(_.age)

// We'll be reusing Bob further on
val bob = User("Bob", 32)

val usersMap = Map(
  "Bob"   -> bob,
  "Alice" -> User("Alice", 23),
  "Ann"   -> User("Ann", 54),
)
// Maps (and most other collections) have a functor
getUserAge(usersMap)

// Either
import cats.syntax.either._

val rightBob  = bob.asRight[String]
val userError = "ERROR".asLeft[User]
getUserAge(rightBob)
getUserAge(userError)

import scala.concurrent.{ExecutionContext, Future}
implicit val ec = ExecutionContext.parasitic

// Future
val futureBob: Future[User] = Future.successful(bob)
getUserAge(futureBob)

// IO
import cats.effect.IO
val bobIo = IO {
  println("Hi! I'm Bob!")
  bob
}

val bobAgeIo = getUserAge(bobIo)

bobAgeIo.unsafeRunSync()

// And on the simple side, Id, the value itself
type Id[T] = T // Also available as cats.Id
Functor[Id]
val bobAge: Int = getUserAge[Id](bob)

// Common operations

rightBob.as("This is a string, there is no Bob here")

val bobEffect = bobIo.void
bobEffect.unsafeRunSync()

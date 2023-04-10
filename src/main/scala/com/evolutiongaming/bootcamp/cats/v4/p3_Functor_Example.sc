import cats.Functor
import cats.effect.unsafe.implicits.global
import cats.syntax.functor._

import scala.concurrent.ExecutionContext

/** Imagine a task to get an age of a user that may exist or may not.
  * And we do not want really to deal with an error handling at this level.
  * What can we do?
  * We can abstract over an effect using F[_] instead of a concrete effect
  * and only demand the presence of Functor instance for F.
  */

final case class User(name: String, age: Int)

val bob: User                = User("Bob", 32)
val users: Map[String, User] = Map(
  "Bob"   -> bob,
  "Alice" -> User("Alice", 23),
  "Ann"   -> User("Ann", 54),
)

// The only thing we care about here is Functor instance for F to be able to call .map function.
def getUserAge[F[_]: Functor](user: F[User]): F[Int] = user.map(_.age)

getUserAge(users)

def maybeUser(name: String): Option[User] =
  users.get(name)

getUserAge(maybeUser("Bob"))
getUserAge(maybeUser("Bop"))

def userOrError(name: String): Either[String, User] =
  users.get(name) match {
    case Some(user) => Right(user)
    case None       => Left("User not found")
  }

getUserAge(userOrError("Bob"))
getUserAge(userOrError("Bop"))

import cats.effect.IO

def ioFindUser(name: String): IO[User] =
  users.get(name) match {
    case Some(user) => IO.pure(user)
    case None       => IO.raiseError(new Throwable("Aw, snap!"))
  }

getUserAge(ioFindUser("Bob")).unsafeRunSync()
getUserAge(ioFindUser("Bop")).attempt.unsafeRunSync()

import scala.concurrent.Future

def futureFindUser(name: String): Future[User] =
  users.get(name) match {
    case Some(user) => Future.successful(user)
    case None       => Future.failed(new Throwable("Aw, snap!"))
  }

implicit val ec = ExecutionContext.parasitic

getUserAge(futureFindUser("Bob"))
getUserAge(futureFindUser("Bop"))

// type Id[T] = T
import cats.Id
def identityUser: Id[User] = bob

getUserAge(identityUser)

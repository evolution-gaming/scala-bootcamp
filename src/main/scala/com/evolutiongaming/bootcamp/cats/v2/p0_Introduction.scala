package com.evolutiongaming.bootcamp.cats.v2

import cats.Monad
import cats.syntax.flatMap._
import cats.syntax.functor._

object p0_Introduction {

  /***
    * Our lecture is dedicated to a library that is widely used within Scala community - Cats.
    * The name `Cats` alludes to Category theory and the library contains abstractions making our life
    * easier.
    * We will see some type classes that are useful in our daily work.
    * Quick re-cap: type classes allow us to extend functionality avoiding limitations of inheritance and in cases when
    * we can not modify the source code.
    *
    * Before peeking into the library, let's have a look at types you already familiar with: Option and Either.
    *
    * Q: what do you find especially useful about Option? Either?
    *
    * Essentially, Option[A] gives a reader of the code a clear and explicit message, that the value of type A may be
    * missing.
    * Thus, one must handle both cases, otherwise compiler (bless it!) would complain by showing a corresponding
    * warnings during compilation.
    * Another handy feature Option provides is ability to chain our computations via .map and .flatMap methods.
    *
    * What about Either?
    * Either[E, A] indicates that given computation may result with an error of type E or with a value of type A.
    * And it's also possible to chain computations using .map and .flatMap methods.
    *
    * An effect in functional programming is a concept of a model that has certain behavior with properties.
    * For example, Option[A] models an effect of optionality of a value, Either[E, A] models an effect of a failure.
    *
    * Consider  a following example:
    */

  final case class User(name: String, level: Int)

  def upgrade(user: User): User = user.copy(level = user.level + 1)

  def findUserOpt(name: String): Option[User] = if (name == "Bob") Some(User(name, 1)) else None
  def saveUserOpt(user: User): Option[Boolean] = Some(true)

  def findUserEither(name: String): Either[String, User] = if (name == "Bob") Right(User(name, 1)) else Left("Not found")
  def saveUserEither(user: User): Either[String, Boolean] = Left("failed to save user")

  /**
    * The logic that finds a user, changes his level and saves may be implemented using option or either:
    */
  val savedOpt: Option[Boolean] = findUserOpt("Bob").map(upgrade).flatMap(saveUserOpt)
  val savedEither: Either[String, Boolean] = findUserEither("Bob").map(upgrade).flatMap(saveUserEither)

  /**
    * But what if we do not want ot opt-in to the concrete implementation?
    * It's the moment when type classes may help us: we demand a presence of Monad type class for our F[_].
    * Thus, we only care about .map and .flatMap implemented for our F.
    * Now we can change the effect without modifying the function implementation.
    */

  def upgradeEffectAgnostic[F[_]: Monad](find: String => F[User], save: User => F[Boolean]): F[Boolean] =
    find("Bob").map(upgrade).flatMap(save)

  val savedAgnOpt: Option[Boolean] = upgradeEffectAgnostic(findUserOpt, saveUserOpt)
  val savedAgnEither: Either[String, Boolean] = upgradeEffectAgnostic(findUserEither, saveUserEither)

    /**
    * P.S. Cats resources:
    * https://typelevel.org/cats/
    * https://www.scalawithcats.com/
    * https://www.scala-exercises.org/cats
    */

}

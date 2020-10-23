package com.evolutiongaming.bootcamp.cats.v2

import cats.effect.IO

object p3_Functor {

  /**
    * In the introduction part we saw three functions with different effects and we wanted a way to compose those functions.
    * A way to do that is to abstract over the concrete effect type and only rely on some certain behaviour of that abstraction.
    * Functor allows us to build chains of calculations within a context without any other details.
    * An instance of Functor for an arbitrary F[_] only tell us that we can call .map on any value `a: F[Foo]`.
    * */
  import cats.Functor

  /**
    * Ex 3.0 implement functor for List (do not use fa.map(f))
    * */
  val listFunctor: Functor[List] = new Functor[List] {
    override def map[A, B](fa: List[A])(f: A => B): List[B] =
      ??? /* your code here */
  }

  /**
    * Ex 3.1 how about Functor for Option, (don not use fa.map(f))
    */
  val optFunctor: Functor[Option] = new Functor[Option] {
    override def map[A, B](fa: Option[A])(f: A => B): Option[B] =
      ??? /* your code here */
  }
    /**
      * Imagine a task to get an age of a user that may exist or may not.
      * And we do not want really to deal with an error handling at this level.
      * What can we do?
      * We can abstract over an effect using F[_] instead of a concrete effect
      * and only demand the presence of Functor instance for F.
      */
    import cats.instances.either._
    import cats.instances.option._
    import cats.syntax.functor._

    final case class User(name: String, age: Int)

    val users = Map(
      "Bob" -> User("Bob", 32),
      "Alice" -> User("Alice", 23),
      "Ann" -> User("Ann", 54)
    )

    // The only thing we care about here is Functor instance for F to be able to call .map function.
    def getUserAge[F[_]: Functor](find: String => F[User])(
      name: String
    ): F[Int] = find(name).map(_.age)

    def asyncFindUser(name: String): IO[User] =
      users.get(name) match {
        case Some(user) => IO.pure(user)
        case None       => IO.raiseError(new Throwable("Aw, snap!"))
      }

    def maybeUser(name: String): Option[User] =
      users.get(name)

    def userOrError(name: String): Either[String, User] =
      users.get(name) match {
        case Some(user) => Right(user)
        case None       => Left("User not found")
      }

    val bobAge: IO[Int] = getUserAge(asyncFindUser)("Bob")
    val aliceAge: Option[Int] = getUserAge(maybeUser)("Alice")
    val annAge: Either[String, Int] = getUserAge(userOrError)("Ann")
}


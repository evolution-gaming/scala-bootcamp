package com.evolutiongaming.bootcamp.cats.v4

object p3_Functor {

  /** Functor is a "map" operation as a type class
    * ote that it's type parameter is higher kind
    */
  import cats.Functor

  /** Ex 3.0 implement functor for Option, (don not use fa.map(f))
    */
  val optFunctor: Functor[Option] = new Functor[Option] {
    override def map[A, B](fa: Option[A])(f: A => B): Option[B] =
      ??? /* your code here */
  }

  /** Ex 3.1 how about Functor for List (do not use fa.map(f))
    */
  val listFunctor: Functor[List] = new Functor[List] {
    override def map[A, B](fa: List[A])(f: A => B): List[B] =
      ??? /* your code here */
  }

  def identity[A](a: A): A = a

  /** Main laws:
    *           fa.map(identity) <-> fa
    *           fa.map(f).map(g) <-> fa.map(a => g(f(a))
    *
    * There's cats-laws module for scalacheck-based law verification
    * https://github.com/typelevel/cats/blob/main/laws/src/main/scala/cats/laws/FunctorLaws.scala
    */
  object IllegalOptionFunctor extends Functor[Option] { // this compiles, but unlawful
    override def map[A, B](fa: Option[A])(f: A => B): Option[B] = None
  }

  // Common operations
  import cats.syntax.either._
  import cats.syntax.functor._

  final case class User(name: String, age: Int)
  val bob: User = User("Bob", 32)

  val bobRight: Either[Throwable, User] = bob.asRight[Throwable] // Right(User(Bob, 32))

  bobRight.as("This is a string, there is no Bob here") // Right("This is a string, there is no Bob here")
  bobRight.map(_ => "This is a string, there is no Bob here")

  val bobVoided: Either[Throwable, Unit] = bobRight.void // Right(())
  bobRight.map(_ => ()) // Right(())

  import cats.effect.IO

  val printAndGetAge: IO[Int] = IO {
    println(s"Bob age is ${bob.age}")
    bob.age
  }

  val printAge: IO[Unit] = printAndGetAge.void
}

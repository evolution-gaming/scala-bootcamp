package com.evolutiongaming.bootcamp.cats.v4

object p4_Applicative {

  /** Applicative defines a `pure` method that allows us to construct an instance of Applicative
    * In cats, Applicative is an ancestor of Apply type class, which defines an `ap` method.
    * The essence of the `ap` method is to apply a function f: A => B in to a value of type A in given context F[_]
    */
  import cats.Applicative

  trait EvoApplicative[F[_]] extends Applicative[F] {
    override def pure[A](x: A): F[A] = ???

    override def ap[A, B](ff: F[A => B])(fa: F[A]): F[B] = ???

    // map two elements
    override def map2[A, B, Z](fa: F[A], fb: F[B])(f: (A, B) => Z): F[Z] =
      map(product(fa, fb))(f.tupled)

    // combine to tuple
    override def product[A, B](fa: F[A], fb: F[B]): F[(A, B)] =
      ap(map(fa)(a => (b: B) => (a, b)))(fb)

    override def map[A, B](fa: F[A])(f: A => B): F[B] =
      ap(pure(f))(fa)

    override def unit: F[Unit] = pure(())
  }

  /** Ex 4.0 implement an Applicative for Option
    */
  val optionApplicative: Applicative[Option] = new Applicative[Option] {
    def pure[A](x: A): Option[A] = ???

    def ap[A, B](ff: Option[A => B])(fa: Option[A]): Option[B] = ???
  }

  /** Ex 4.1 implement an Applicative for Map
    */
  type MapF[A] = Map[A, A]

  val mapApplicative: Applicative[MapF] = new Applicative[MapF] {
    def pure[A](x: A): MapF[A] = ???

    def ap[A, B](ff: MapF[A => B])(fa: MapF[A]): MapF[B] = ???
  }

  // Common operations
  import cats.effect.IO
  import cats.syntax.applicative._
  import cats.syntax.apply._
  import cats.syntax.option._

  40.pure[Option] // Some(40)

  // mapN
  Applicative[Option].map2(1.some, 3.some)(_ + _)
  (1.some, 3.some).mapN(_ + _)
  (1.some, none[Int], 3.some).mapN(_ + _ + _)

  // *>, also known as productR
  // Evaluates both args, keeps value from the right one
  // Commonly used for IO-like effect types
  val loggingOne: IO[Int] = IO(println("Computing!")) *> IO(1)

  // It can be used outside of IO, it's just rarely useful there
  none[Int] *> 2.some
}

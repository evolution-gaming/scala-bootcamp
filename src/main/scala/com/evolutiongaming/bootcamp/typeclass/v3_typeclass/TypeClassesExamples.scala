package com.evolutiongaming.bootcamp.typeclass.v3_typeclass

object TypeClassesExamples extends App {

  // 1. Semigroup
  // 1.1. Implement all parts of the typeclass definition
  trait Semigroup[A] {
    def combine(x: A, y: A): A
  }

  // 1.2. Implement Semigroup for Long, String

  // 1.3. Implement combineAll(list: List[A]) for non-empty lists

  // combineAll(List(1, 2, 3)) == 6

  // 1.4. Implement combineAll(list: List[A], startingElement: A) for all lists

  // combineAll(List(1, 2, 3), 0) == 6
  // combineAll(List(), 1) == 1

  // 2. Monoid
  // 2.1. Implement Monoid which provides `empty` value (like startingElement in previous example) and extends Semigroup

  // 2.2. Implement Monoid for Long, String

  // 2.3. Implement combineAll(list: List[A]) for all lists

  // combineAll(List(1, 2, 3)) == 6

  // 2.4. Implement Monoid for Option[A]

  // combineAll(List(Some(1), None, Some(3))) == Some(4)
  // combineAll(List(None, None)) == None
  // combineAll(List()) == None

  // 2.5. Implement Monoid for Function1 (for result of the function)

  // combineAll(List((a: String) => a.length, (a: String) => a.toInt))        === (a: String) => (a.length + a.toInt)
  // combineAll(List((a: String) => a.length, (a: String) => a.toInt))("123") === 126

  // 3. Functor
  trait Functor[F[_]] {
    def map[A, B](fa: F[A])(f: A => B): F[B]
  }

  object Functor {
    def apply[F[_] : Functor]: Functor[F] = implicitly[Functor[F]]
  }

  implicit class FunctorOps[F[_]: Functor, A](fa: F[A]) {
    def map[B](f: A => B): F[B] = Functor[F].map(fa)(f)
  }

  implicit val optionFunctor: Functor[Option] = new Functor[Option] {
    def map[A, B](fa: Option[A])(f: A => B): Option[B] = fa.map(f)
  }

  // 3.1. Implement Functor for Map values

  // 4. Semigroupal
  // 4.1. Semigroupal provides `product` method,
  // so in combination with Functor we'll be able to call for example `plus` on two Options (its content)
  trait Semigroupal[F[_]] {
    def product[A, B](fa: F[A], fb: F[B]): F[(A, B)]
  }

  // 4.2. Implement Summoner for Semigroupal

  // 4.3. Implement Syntax for Semigroupal, so later you'll be able to do:
  // (Option(1) product Option(2)) == Some((1, 2))

  // 4.4. Implement Semigroupal for Option

  // 4.5. Implement `mapN[R](f: (A, B) => R): F[R]` extension method for Tuple2[F[A], F[B]]

  // (Option(1), Option(2)).mapN(_ + _) == Some(3)
  // (Option(1), None).mapN(_ + _)      == None

  // 4.6. Implement Semigroupal for Map

  // (Map(1 -> "a", 2 -> "b"), Map(2 -> "c")).mapN(_ + _) == Map(2 -> "bc")

  // 5. Applicative
  trait Applicative[F[_]] extends Semigroupal[F] with Functor[F] {
    def pure[A](x: A): F[A]
  }

  object Applicative {
    def apply[F[_] : Applicative]: Applicative[F] = implicitly
  }

  implicit class ApplicativeValueOps[F[_] : Applicative, A](a: A) {
    def pure: F[A] = Applicative[F].pure(a)
  }

  // 5.1. Implement Applicative for Option, Either

  // 5.2. Implement `traverse` function
  def traverse[A, B](as: List[A])(f: A => Option[B]): Option[List[B]] = ???

  // traverse(List(1, 2, 3)) { i =>
  //   Option.when(i % 2 == 1)(i)
  // } == None

  // traverse(List(1, 2, 3)) { i =>
  //   Some(i + 1)
  // } == Some(List(2, 3, 4))

  // 5.3. Implement `traverseA` for all Applicatives instead of Option

  // traverseA(List(1, 2, 3)) { i =>
  //   Either.cond(i % 2 == 1, i, "Error")
  // } == Left("Error")

  // traverseA(List(1, 2, 3)) { i =>
  //   Right(i + 1): Either[Int, Any]
  // } == Right(List(2, 3, 4))


  // Scala Typeclassopedia: https://github.com/lemastero/scala_typeclassopedia
}

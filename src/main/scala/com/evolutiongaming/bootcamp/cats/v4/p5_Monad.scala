package com.evolutiongaming.bootcamp.cats.v4

import cats.FlatMap
import cats.effect.unsafe.implicits.global
import cats.effect.{ExitCode, IO}

import scala.concurrent.duration._

object p5_Monad {

  /** If we speak about monads in functional programming it would be more or less safe to say that anything,
    * that implements two operations `bind` and `unit` and satisfies three monadic laws is a monad.
    * In Scala `Unit` is called `pure`, and `bind` is a `flatMap`.
    *
    * Monad is a type with "flatMap" and "pure" satisfying certain laws.
    *
    * Laws:
    *      Left identity: pure(a).flatMap(f) <-> f(a)
    *      Right identity: fa.flatMap(pure) <-> fa
    *      Associativity: fa.flatMap(f).flatMap(g) <-> fa.flatMap(a => f(a).flatMap(g))
    */

  import cats.Monad

  // There's FlatMap class with just flatMap without pure, most syntax operations are there
  trait EvoMonad[F[_]] /* extends Monad[F] */ {
    def pure[A](a: A): F[A]

    def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]

    /** Ex 5.0 implement map in terms of pure and flatMap.
      */
    def map[A, B](fa: F[A])(f: A => B): F[B] = ??? /* your code here */

    // cats only special method for stack-safety
    // def tailRecM[A, B](a: A)(f: A => F[Either[A, B]]): F[B] = ???
  }

  /** You already encountered some monads, for example `Option[A]` is a monad that models an effect
    * of optionality of a value of type A, `Either[E, A]` models an effect which evaluation
    * may fail with an error of type `E` or succeed with a value of type `A`.
    *
    * In more general sense, monad is about sequencing computations.
    * Having `flatMap` and `map` we can now chain our computations. For example:
    */
  trait User
  trait GiftCard
  trait Gift

  def findUser(name: String): Option[User]       = ???
  def findGiftCard(user: User): Option[GiftCard] = ???
  def pickGift(card: GiftCard): Option[Gift]     = ???

  lazy val maybeGift1: Option[Gift] =
    findUser("Bob").flatMap(findGiftCard).flatMap(pickGift)

  // Or, we can use `for-comprehension`:
  lazy val maybeGift2: Option[Gift] = for {
    user <- findUser("Bob")
    card <- findGiftCard(user)
    gift <- pickGift(card)
  } yield gift

  /** This allows us to describe the whole computation as if we have all the parts in place despite
    * the fact that user, card, or gift may be missing. No null checks etc.
    */

  /** On associativity: lets de-sugar maybeGift2.
    * Associativity law requires these two to be equivalent:
    * fa.flatMap(f).flatMap(g) <-> fa.flatMap(a => f(a).flatMap(g))
    */

  /** Ex 5.1 implement EvoMonad for List
    */
  val listMonad: EvoMonad[List] = new EvoMonad[List] {
    override def pure[A](a: A): List[A] = ???

    override def flatMap[A, B](fa: List[A])(f: A => List[B]): List[B] = ???
  }

  /** Ex 5.2 implement EvoMonad for Option
    */
  val optionMonad: EvoMonad[Option] = new EvoMonad[Option] {
    override def pure[A](a: A): Option[A] = ???

    override def flatMap[A, B](fa: Option[A])(f: A => Option[B]): Option[B] = ???
  }

  /** Cats provides number of default instances
    */

  val maybeInt: Option[Int] = Monad[Option].pure(1)
  import cats.syntax.applicative._
  1.pure[Option]

  Monad[Option].pure("Hi")
  Monad[List].flatMap(List("Hie"))((v: String) => List(v, v, v))
  Monad[cats.data.NonEmptyList]

  // Monad[Map[Int, *]]
  FlatMap[Map[Int, *]]

  /** When we work with a concrete Monad like List or Option we can call .flatMap .pure or .map directly.
    * But what if we have some abstract effect F[_]?
    * As we have seen before, we can demand an instance of a certain type class to be present in the implicit context.
    * And we need also to import syntax for certain operations:
    */

  import cats.syntax.flatMap._
  import cats.syntax.functor._ // map

  def exec[F[_]: Monad](log: String => F[Unit], action: F[Unit]): F[ExitCode] =
    log("Feels good").flatMap(_ => action).map(_ => ExitCode.Success)

  /** Monad is also an Applicative, and Applicative's behavior must be consistent with Monad (type class coherence).
    * If a type has a Monad, Applicative for that type should behave as it was derived from Monad.
    * https://impurepics.com/posts/2019-03-18-monad-applicative-consistency-law.html
    *
    * This forbids some behaviors, e.g. error accumulation in Either.
    */

  final case class Player(name: String, age: Int)

  val bob: Player   = Player("Bob", 32)
  val alice: Player = Player("Alice", 23)

  def minAge[F[_]: Monad](fa: F[Player], fb: F[Player]): F[Int] = {
    for {
      a <- fa
      b <- fb // Computation cannot proceed here without an A
    } yield a.age.min(b.age)
  }

  import cats.syntax.either._

  minAge(bob.rightNel[String], alice.rightNel[String])
  minAge(bob.rightNel[String], "ERROR".leftNel[Player])
  minAge("ERROR".leftNel[Player], "ALSO ERROR".leftNel[Player])

  // import cats.syntax.validated._
  // minAge("ERROR".invalidNel[Player], "ALSO ERROR".invalidNel[Player])

  // Other operations

  // There's >>, behaves like *>, but takes right argument by-name
  // https://impurepics.com/posts/2019-02-09-operator-wars-reality.html

  import cats.effect.IO

  IO(println("Computing")) >> IO(1)
}

object p5_MonadRun extends App {
  def putStrLine(s: String): IO[Unit] = IO(println(s)) *> IO.sleep(1.second)

  def program: IO[Unit] = putStrLine("forever") >> program

  program.unsafeRunSync()
}

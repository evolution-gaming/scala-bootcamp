package com.evolutiongaming.bootcamp.testing2

import cats.Monad
import cats.syntax.all._
import cats.tagless.finalAlg
import eu.timepit.refined._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.numeric._
import eu.timepit.refined.string._
import org.scalatest.funsuite.AnyFunSuite

// *Introduction*
//
// The bug which is not representable using the code will never happen.
// The simplest example of such defense is introduction of type system. I.e.
// if you have marked your field as `Integer` in your Java app, we do not need
// to test if it contains `String` inside. It simply cannot.
//
// Scala is much more powerful in that area, i.e., for example, we can make
// compile check if number is positive, if string is an actual e-mail etc. which
// is called refined types (https://github.com/fthomas/refined). We can also
// make sure the part of the code never accesses the database unless asked to do
// so etc., which is called effect tracking (https://typelevel.org/cats-effect/).
//
// It is not just a cool rocket science tech, we are using this stuff every day,
// and most Scala developers here won't be surprised if you ask them about it.
object PowerfulScala {

  // Exercise 1
  //
  // Prove Scala is at least as powerful as Java. Make sure that we cannot
  // call `energy("wrong stuff")`. You will also have to change
  // "we got a correct result" test, because it accepts `String` now.
  //
  // Run the suite using the command below:
  //
  // sbt:scala-bootcamp> testOnly *testing2.PowerfulScalaSpec
  //
  def energy(mass: String): String = {
    val speedOfLight = BigDecimal(299792458)
    val energy = BigDecimal(mass) * speedOfLight.pow(2)
    energy.toString
  }

}
class PowerfulScalaSpec extends AnyFunSuite {

  test("we get a correct result") {
    assert(PowerfulScala.energy("100") == "8987551787368176400")
  }
  test("wrong call does not compile") {
    assertTypeError("""PowerfulScala.energy("wrong stuff")""")
  }

}

object RefinedScala {

  // As part of learning Algebraic Data Types, you learned another useful technique
  // how to avoid bugs without having the actual unit tests: smart constructors.
  //
  // Like these:
  case class PositiveNumber private (val value: Int) extends AnyVal
  object PositiveNumber {
    def create(value: Int): Option[PositiveNumber] =
      if (value > 0) Some(PositiveNumber(value)) else None
  }

  // The problem about smart constructors and value classes is that you create a
  // new type in Scala 2 (it is fixed in Scala 3), so you have to wrap all
  // the operations or use some evil methods such as implicit conversions.
  //
  // Refined types to the rescue!
  //
  // There is a library allowing to check the properties of the types during
  // compilation, i.e you have the same good old types, but with limitations:
  case class DatabaseConfig(
    host: String Refined IPv4,
    timeoutMilliseconds: Int Refined NonNegative
  )

  // You can do this:
  val config = DatabaseConfig(host = "127.0.0.1", timeoutMilliseconds = 16)
  val timeoutInSeconds = config.timeoutMilliseconds / 1000

  // But you cannot do any of these (try uncommenting them):
  // DatabaseConfig(host = "127A.0.0.1", timeoutMilliseconds = 16)
  // DatabaseConfig(host = "127.0.0.1", timeoutMilliseconds = -16)

  // Exercise 2
  //
  // Prove Scala is at more powerful than Java. Make sure that we cannot
  // we cannot represent a wrong XML document using the case class by
  // using `Url` and `Xml` refinements.
  //
  // sbt:scala-bootcamp> testOnly *testing2.RefinedScalaSpec
  //
  case class Document(url: String, body: String)

}
class RefinedScalaSpec extends AnyFunSuite {

  test("wrong call does not compile") {
    RefinedScala.Document(
      url = "https://developer.mozilla.org/en-US/docs/Web/API/Document_Object_Model/Examples",
      body = "<complete/>"
    )
    assertTypeError("""RefinedScala.Document("wrong url","<incomplete")""")
  }

}
object Parametricity {

  // Exercise 3
  //
  // You, probably, heard about "parametric reasoning" previously during these
  // lectures. Let's repeat the material a bit again.
  //
  // There is a famous paper by Philop Walder called "Theorems for free!"
  //
  // Quote:
  // > Write down the definition of a polymorphic function on a piece of paper.
  // > Tell me its type, but be careful not to let me see the function’s
  // > definition. I will tell you a theorem that the function satisfies.
  //
  // Let's do some exercise to understand the concept (shamelessly stolen
  // from the article of Daniel Sebban)

  // Implement the following function in all possible ways:
  def f1_way1[A](a: A): A = ???
  def f1_way2[A](a: A): A = ???

  // Let's do another one...
  def f2_way1[A](a: A, b: A): A = ???
  def f2_way2[A](a: A, b: A): A = ???
  def f2_way3[A](a: A, b: A): A = ???

  // Can this function use `a` somehow in implementation?
  def f3[A](a: A, b: Int): Int = ???

  // How about this one?
  def f4[A](a: A, b: String): String = ???

  // Implement the following function in several ways:
  // What is common in all of these implementations?
  def f5_way1[A](as: List[A]): List[A] = ???
  def f5_way2[A](as: List[A]): List[A] = ???
  def f5_way3[A](as: List[A]): List[A] = ???

  // How many ways we can implement this function with?
  def f6[A, B](as: List[A]): List[B] = ???

  // How about this one?
  def f7[A](a: A): Int = ???

  // Exercise 4
  //
  // How can we use in real life besides creating puzzles for students?
  //
  // Less possibilities of implementations = less possibilities of bugs.
  // We only specify in types what we want to know adhering to so called
  // "Rule of least power".
  //
  // Try to break the functions below (so they return wrong results sometimes),
  // but still pass the test. Run the tests like following:
  //
  // sbt:scala-bootcamp> testOnly *testing2.ParametricitySpec
  //
  def reversed1(list: List[Int]): List[Int] = list.reverse

  def reversed2[A](list: List[A]): List[A] = list.reverse

  def reversed3[T](list: T, reverse: T => T): T = reverse(list)

  // reversed3 does not look like real at all!
  // can we make it more convenient?
  //
  // yes, we can, can you break this function without breaking the test?

  def reversed4[T](list: T)(implicit reversable: Reversable[T]): T = reversable.reverse(list)

  // we need this boilerplate for this to work
  // often generated by libraries / macros:

  trait Reversable[T] { def reverse(a: T): T }
  implicit val listReversable: Reversable[List[Int]] = _.reverse

  // still, even if `Reversable` is implemented by library (JSON libraries love doing it)
  // it looks quite verbose, can we do less verbose?
  //
  // the approach is so popular there is a special syntax for it!

  def reversed5[T: Reversable](list: T): T = implicitly[Reversable[T]].reverse(list)

  // still too verbose?
  // libraries usually provide some more convenient methods of summoning it

  def reversed6[T: Reversable](list: T): T = Reversable[T].reverse(list)

  object Reversable {
    def apply[T](implicit reversable: Reversable[T]): Reversable[T] = reversable
  }

  // still unhappy?
  // they usually also provide a syntax
  def reversed7[T: Reversable](list: T): T = list.reverse

  implicit class ReversableSyntax[T](val self: T) extends AnyVal {
    def reverse(implicit reversable: Reversable[T]): T = reversable.reverse(self)
  }

  // do we need any tests for reversed3 - reversed7 at all?

}
class ParametricitySpec extends AnyFunSuite {

  test("reversed1 works correctly") {
    assert(Parametricity.reversed1(Nil) == Nil)
    assert(Parametricity.reversed1(List(1, 2, 3, 4, 5)) == List(5, 4, 3, 2, 1))
  }
  test("reversed2 works correctly") {
    assert(Parametricity.reversed2(Nil) == Nil)
    assert(Parametricity.reversed2(List(1, 2, 3, 4, 5)) == List(5, 4, 3, 2, 1))
  }
  test("reversed3 works correctly") {
    def reverse(list: List[Int]) = list.reverse
    assert(Parametricity.reversed3(List.empty[Int], reverse) == Nil)
    assert(Parametricity.reversed3(List(1, 2, 3, 4, 5), reverse) == List(5, 4, 3, 2, 1))
  }
  test("reversed4 works correctly") {
    assert(Parametricity.reversed4(List.empty[Int]) == Nil)
    assert(Parametricity.reversed4(List(1, 2, 3, 4, 5)) == List(5, 4, 3, 2, 1))
  }
  test("reversed5 works correctly") {
    assert(Parametricity.reversed5(List.empty[Int]) == Nil)
    assert(Parametricity.reversed5(List(1, 2, 3, 4, 5)) == List(5, 4, 3, 2, 1))
  }
  test("reversed6 works correctly") {
    assert(Parametricity.reversed6(List.empty[Int]) == Nil)
    assert(Parametricity.reversed6(List(1, 2, 3, 4, 5)) == List(5, 4, 3, 2, 1))
  }
  test("reversed7 works correctly") {
    assert(Parametricity.reversed7(List.empty[Int]) == Nil)
    assert(Parametricity.reversed7(List(1, 2, 3, 4, 5)) == List(5, 4, 3, 2, 1))
  }

}
object EffectTracking {

  // Exercise 5
  //
  // We _can_ actually break all the methods above easily with doing some evil
  // stuff. I.e., for example, we could do VW style code (see also
  // https://github.com/auchenberg/volkswagen).
  //
  // I.e., we could record number of tests we did in some external variable and
  // only stop working properly after 100 runs. Or we could just check the time
  // and fail after specific time passed. Or we can be even more evil, and make
  // sure we check some external URL and if it says to fail, we would fail.
  //
  // All these evil things we could do are called effects. Is it possible to
  // prevent effects to happen during compile time? Turns out that we certain
  // discipline we can do it. One technique is called effect tracking.
  //
  // We agree (or check using a static checker) that we do not do effects in
  // the code. Then, when we really need to do an effect, we pass the effect
  // as dependency.
  //
  // Another cool part is that writing unit tests becomes really easy.

  trait Printing {
    def print(text: String): Unit
  }
  trait Clock {
    def currentTimeMillis(): Long
  }
  trait Variable {
    def set(x: Long): Unit
    def get(): Option[Long]
  }
  class Service(printing: Printing, clock: Clock, variable: Variable) {
    def call: Unit = {
      val currentTime = clock.currentTimeMillis()
      val previousTime = variable.get()
      previousTime map { previousTime =>
        val timePassed = currentTime - previousTime
        if (timePassed > 1000) printing.print("A second passed since first call!")
      } getOrElse {
        variable.set(currentTime)
      }
    }
  }

}
object EffectTrackingSpec extends AnyFunSuite {

  // Implement the tests validating `Service` functionality.
  //
  // Run the tests like following:
  //
  // sbt:scala-bootcamp> testOnly *testing2.ParametricitySpec
  //
  // Bonus task: implement them using `State` monad mentioned in `UnitTesting` section.
  test("Service.call prints out correct message after a second") {
    ???
  }
  test("Service.call does not print a message before a second passes") {
    ???
  }

}
object CatsTagless {

  // Was not it tedious to write the code like that in a previous exercise?
  // We cold use our `Reversable` trick and put the stuff
  //
  // As previously mentioned, there are libraries and techniques to ease our pain.
  // They autogenerate most of the stuff for us. We will not go through them this
  // time, but I just wanted to show how cool does it look.

  @finalAlg
  trait Printing[F[_]] {
    def print(text: String): F[Unit]
  }
  @finalAlg
  trait Clock[F[_]] {
    def currentTimeMillis(): F[Long]
  }
  @finalAlg
  trait Variable[F[_]] {
    def set(x: Long): F[Unit]
    def get(): F[Option[Long]]
  }
  class Service[F[_]: Monad: Printing: Clock: Variable] {
    def call: F[Unit] = for {
      currentTime <- Clock[F].currentTimeMillis()
      previousTime <- Variable[F].get()
      _ <- previousTime map { previousTime =>
        val timePassed = currentTime - previousTime
        if (timePassed > 1000) Printing[F].print("A second passed since first call!") else ().pure[F]
      } getOrElse {
        Variable[F].set(currentTime)
      }
    } yield ()
  }

}

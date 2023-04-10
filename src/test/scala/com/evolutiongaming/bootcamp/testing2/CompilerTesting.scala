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
import scala.util.Try
import scala.util.Failure
import scala.util.Success

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
    val energy       = BigDecimal(mass) * speedOfLight.pow(2)
    energy.toString
  }
}

class PowerfulScalaSpec extends AnyFunSuite {

  test("we get a correct result") {
    assert(PowerfulScala.energy("100") == "8987551787368176400")
  }

  test("wrong call does not compile") {
    // assertTypeError is a special method which allows to check if the code snippet does not compile
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
  // Refined types to the rescue! https://github.com/fthomas/refined
  //
  // There is a library allowing to check the properties of the types during
  // compilation, i.e you have the same good old types, but with limitations:
  case class DatabaseConfig(
    host: String Refined IPv4,
    timeoutMilliseconds: Int Refined NonNegative,
  )

  // You can do this:
  val config           = DatabaseConfig(host = "127.0.0.1", timeoutMilliseconds = 16)
  val timeoutInSeconds = config.timeoutMilliseconds / 1000

  // But you cannot do any of these (try uncommenting them):
  // DatabaseConfig(host = "127A.0.0.1", timeoutMilliseconds = 16)
  // DatabaseConfig(host = "127.0.0.1", timeoutMilliseconds = -16)

  // It is also possible to perform validation at runtime using `refine` macros:
  // as you can see, it returns `Either` with the possible error case
  val hostFromUserInput: String                   = ""
  val host: Either[String, Refined[String, IPv4]] =
    refineV[IPv4](hostFromUserInput)

  // It is possible to pass values of more specific types as more general type
  val largerThanSix: Int Refined Greater[6] = 7
  val posInt: Int Refined Positive          = largerThanSix

  // Exercise 2
  //
  // Make sure that we cannot represent a wrong XML document using the case class by
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
      body = "<complete/>",
    )
    assertTypeError("""RefinedScala.Document("wrong url","<incomplete")""")
  }
}

object ValueClasses {
  // Let's go back to value classes and smart constructors.
  // Often, they're good enough for one's usecase - they help to avoid bugs and make the code more readable
  // without introducing too much cognitive overhead to the codebase.

  // There are many benefits of using value classes:
  // - provide additional net of type safety
  // - allow to use more meaningful names for types
  // - provide a space when one can document the code
  // - help maintain invariants

  // example 0
  case class Account0(name: String, number: String)
  val invalidAccount1 = Account0("123456789", "John Doe")
  // using name parameters might help to avoid bugs, but it's just visual aid,
  // it won't stop incorrect code from compiling
  val invalidAccount2 = Account0(name = "123456789", number = "John Doe")

  // example 1
  case class Account(
    name: AccountName,
    number: AccountNumber,
    balance: BigDecimal,
  )
  case class AccountName private (value: String) extends AnyVal

  /** Account number is a string in IBAN format, complying with the ISO 13616
    * standard. The IBAN consists of up to 34 alphanumeric characters, as
    * follows:
    *   - country code using 'ISO 3166-1 alpha-2' - 2 letters
    *   - check digits – two digits
    *   - Basic Bank Account Number (BBAN) – up to 30 alphanumeric characters
    *     that are country-specific.
    *
    * Example: IE12 BOFI 9000 0112 3456 78
    */
  case class AccountNumber private (value: String) extends AnyVal
  object AccountNumber {
    // smart constructor which performs validation
    // for the sake of simplicity, we do not use it in examples
    def create(value: String): Option[AccountNumber] =
      // example of validation:
      // There is connection between the regex and documentation above,
      // it is clear, why such regex was used and what it does.
      // There is even wiki page avout validating IBANs
      // https://en.wikipedia.org/wiki/International_Bank_Account_Number#Validating_the_IBAN
      if (value.matches("[A-Z]{2}[0-9]{2}[A-Z0-9]{30}"))
        Some(AccountNumber(value))
      else None
  }

  val accountName   = AccountName("John Doe")
  val accountNumber = AccountNumber("123456789")

  // compiler will not let you compile this code
  // val invalidAccount3 = Account(accountNumber, accountName)

  val account = Account(accountName, accountNumber, 0)

  // example 3

  // We have 3 values represented by BigDecimal: stake, winRate and winAmount. What do they mean?
  // We can copy paste scaladoc to every usage of these values, but it's not very readable.

  /** @param stake
    *   \- amount of money player bet
    * @param winRate
    *   \- how much player will win per 1 coin staked
    * @return
    *   \- amount of money which should be paid out to the player
    */
  def calculateWin(
    stake: BigDecimal,
    winRate: BigDecimal,
  ): BigDecimal = stake * winRate

  // We can add smart constructor if needed.
  // There is single space for scaladoc, so it's easier to maintain documentation.

  /** Amount of money player bet. It's used for calculating win amount. */
  case class Stake(value: BigDecimal)   extends AnyVal
  case class WinRate(value: BigDecimal) extends AnyVal

  case class WinAmount(value: BigDecimal) extends AnyVal
  object WinAmount {
    def apply(stake: Stake, winRate: WinRate): WinAmount =
      WinAmount(stake.value * winRate.value)
  }

  // Now we can even omit scaladoc completely. If necessary, one can take a look at `Stake`, `WinRate` and `WinAmount` classes.
  // Isn't this more readable?
  def calculateWin2(
    stake: Stake,
    winRate: WinRate,
  ): WinAmount = WinAmount(stake, winRate)

  // example 4

  // Let's focus on what parameters we can pass to the `transferMoney` function
  def transferMoney(from: Account, to: Account, amount: BigDecimal): Unit = {
    // some logic
    // val deducted = from subtract...
    // val added = to add ...
  }

  // sending money is really important, so we want to make sure we don't make any mistakes
  // what if we, by accident, pass the wrong parameters to the `transferMoney` function?
  val from: Account = ???
  val to: Account   = ???

  // Exercise

  // what is wrong with following code?
  // how can we make sure that we don't make such mistakes?
  transferMoney(to, from, 100)

}

// One can't make a bug, if it is impossible to express invalid state using the code
object ImpossibleState {

  // What is wrong with following class?
  // Can you find a potential risk?
  final case class Task0(
    id: String,
    isFinished: Boolean,
    finishedAt: Option[Long],
    progress: Double,
    isCancelled: Boolean,
  )

  // Exercise 3
  // Use Algebraic Data Types to model Task domain.
  sealed trait Task

  // Does one needs to write tests for such code?
  // What kind of tests are not needed anymore after using ADTs?
}

object Parametricity {

  // Exercise 4
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
  // Let's do some exercise to understand the concept
  // Examples are from Daniel Sebban's blogpost:
  // https://medium.com/bigpanda-engineering/understanding-parametricity-in-scala-520f9f10679a

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

  // Exercise 5
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

  def reversed4[T](list: T)(implicit reversable: Reversable[T]): T =
    reversable.reverse(list)

  // we need this boilerplate for this to work
  // often generated by libraries / macros:

  trait Reversable[T] { def reverse(a: T): T }
  implicit val listReversable: Reversable[List[Int]] = _.reverse

  // still, even if `Reversable` is implemented by library (JSON libraries love doing it)
  // it looks quite verbose, can we do less verbose?
  //
  // the approach is so popular there is a special syntax for it!

  def reversed5[T: Reversable](list: T): T =
    implicitly[Reversable[T]].reverse(list)

  // still too verbose?
  // libraries usually provide some more convenient methods of summoning it

  def reversed6[T: Reversable](list: T): T = Reversable[T].reverse(list)

  object Reversable {
    // smart trick to avoid writing `implicitly` everywhere
    // e.g. implicitly[Reversable[T]] can be replaced by Reversable[T]
    def apply[T](implicit reversable: Reversable[T]): Reversable[T] = reversable
  }

  // still unhappy?
  // they usually also provide a syntax
  def reversed7[T: Reversable](list: T): T = list.reverse

  implicit class ReversableSyntax[T](private val self: T) extends AnyVal {
    // extension method which provides `reverse` method to any type which has `Reversable` instance
    def reverse(implicit reversable: Reversable[T]): T =
      reversable.reverse(self)
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
    assert(
      Parametricity.reversed3(List(1, 2, 3, 4, 5), reverse) == List(5, 4, 3, 2, 1)
    )
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

  // Exercise 6
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

  // Take a look at this service
  // how to test that it works correctly? If it fetches time and prints message?
  class CoupledService() {
    def call(arg: String): Unit = {
      // ... many lines of code
      val currentTime = System.currentTimeMillis()
      val msg         = s"$currentTime $arg"
      // ... many lines of code

      print(msg)
    }
  }

  // Inject dependencies which are responsible for effectful operations

  // avoid traits which do multiple things
  // single-responsibility principle - it's easier to provide test instances for such smaller, well defined traits
  trait PrintWithClock {
    def print(text: String): Unit
    def currentTimeMillis(): Long
  }

  trait Printing {
    def print(text: String): Unit
  }

  trait Clock {
    def currentTimeMillis(): Long
  }

  // Printing and Clock can be accepted as constructor arguments and use them instead of System ones.
  // There can be 2 implementations now:
  // - prod one which uses System
  // - test one which uses some fake clock and fake printing
  class Service(printing: Printing, clock: Clock) {
    def call(arg: String): Unit = {
      val currentTime = clock.currentTimeMillis()
      val msg         = s"$currentTime $arg"

      printing.print(msg)
    }
  }

  object Service {
    val default = new Service(
      text => print(Service),
      () => System.currentTimeMillis(),
    )
  }

}
object EffectTrackingSpec extends AnyFunSuite {

  // Implement the tests validating `Service` functionality.
  //
  // Run the tests like following:
  //
  // sbt:scala-bootcamp> testOnly *testing2.ParametricitySpec
  //
  test("Service.call prints out anything") {
    ???
  }

  test("Service.call prints out correct message with current time") {
    ???
  }

}

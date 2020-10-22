package com.evolutiongaming.bootcamp.typeclass

import scala.language.implicitConversions
import scala.util.Random

object Implicits {
  /*
  Brevity is the soul of wit.

  No one likes to repeat himself.
  Why to repeat something which should be obvious from the context (allegedly)?
  To ponder to some really lazy coders out there, Scala offers quite a unique language feature - implicits.
   */

  /*
  1. Implicit conversions

  Have you even wondered why we can't just convert all the numbers to strings
  automagically? Today is your lucky day!
   */
  object ImplicitConversions {
    //implicit conversions defined in the lexical scope
    implicit def intToString(value: Int): String = value.toString

    def repeatStrTwice(string: String): String = string * 2

    /*
    Here we have a String arg required but we passed an Int. Before stopping compilation with an error, the
    Scala compiler tries to find an implicit conversion functions chain which would give us Int => String.
     */
    //equivalent to repeatStrTwice(intToString(42))
    repeatStrTwice(42) //If God does not exist, everything is permitted
  }

  /*
  2. Extension methods using implicit conversion functions

  When you want to add a method to some existing type, sometimes you are so lazy that you don't want to
  change the type itself (could be quite inconvenient to do that for a Scala standard library type).
   */
  object ExtensionMethods {
    class YoloRichString(inner: String) {
      def yolo(): String = if (Random.nextBoolean()) inner else "YOLO"
    }

    object Implicits {
      implicit def stringToYoloRichString(value: String): YoloRichString = new YoloRichString(value)
    }

    object Workspace {
      //implicit conversion coming from an import
      import Implicits._

      def veryImportantPieceOfWork(): Unit = {
        println(
          /*
          String doesn't have "def yolo()" method. What are we going to do?
          Find an implicit conversion String => T, where T has the right method!
           */
          //equivalent to stringToYoloRichString("my very important value").yolo()
          //which is the same as new YoloRichString("my very important value").yolo()
          "my very important value".yolo()
        )
      }
    }
  }

  /*
  3. Implicit classes

  I'm too lazy to define class + implicit conversion function for my extension methods.
  Implicit classes help you write even less!
   */
  object ImplicitClasses {

    import scala.concurrent.duration._

    object Implicits {
      /*
      An implicit class is a class marked with the
      implicit keyword. This keyword makes the class’s primary constructor available for implicit conversions
      when the class is in scope.
       */
      implicit class RichFiniteDuration(inner: FiniteDuration) {
        def plus99Days: FiniteDuration = inner.plus(99.days)
      }
    }

    object Workspace {
      //implicit class coming from an import
      import Implicits._

      /*
      Equivalent to new RichFiniteDuration(1.day).plus99Days
      In most cases Scala compiler can actually elide instantiation of RichFiniteDuration and inline
      the extension method in-place.
       */
      1.day.plus99Days
    }
  }

  /*
  Exercise 1.

  Let us define an extension method for java.time.Instant (point-in-time UTC timestamp) which checks if
  the timestamp is before the Common Era (BCE) or not:

  def isBce: Boolean = ???

  Usage: Instant.now().isBce
   */
  object Exercise1 {
    import java.time.Instant

    val CommonEraStart: Instant = Instant.parse("0000-01-01T00:00:00.000Z")

    object Implicits {
      //put your implicit class or implicit conversion function here
    }

    object Workspace {
      //use isBce extension method to implement this one
      def isCe(instant: Instant): Boolean = ???
    }
  }

  /*
  4. Implicit parameters

  Hiding conversion function calls in plain sight and adding random methods to standard library classes
  was fun. What else? Magic function parameters!
   */
  object ImplicitParameters {
    case class MagicKey(value: String)
    trait MagicPotion

    /*
    The last parameter list of a method can be marked as implicit - in this case the last parameter list can
    be omitted when the method is called, given that required implicit values can be obtained from the scope.
     */
    //just one implicit parameters list
    def openTheBox(implicit key: MagicKey): String = s"Magic box opened with $key"

    //multiple parameter lists with multiple implicit parameters in the end
    def putNumbersInTheBox(
      number1: Int,
    )(
      number2: Int,
    )(implicit
      key: MagicKey,
      potion: MagicPotion,
    ): String = s"Number $number1 and $number2 have been put in the magic box using $key and $potion"

    object Implicits {
      //implicit values can be a 'val'
      implicit val magicKey: MagicKey = MagicKey("open-sesame")
      //could be an object
      implicit object TheMagicPotion extends MagicPotion
      //even could be a def! but more on that later
    }

    object Workspace {
      import Implicits._

      lazy val myStory: Vector[String] = Vector(
        openTheBox, //equivalent to openTheBox(Implicits.magicKey)
        putNumbersInTheBox(1)(2),
        //= putNumbersInTheBox(1)(2)(Implicits.magicKey, Implicits.TheMagicPotion),

        //implicit parameters can also be passed directly:
        openTheBox(MagicKey("another-key")),
      )
    }
  }

  /*
   * 5. More implicit parameters
   *
   * Could I use generic types for implicit parameters? Sure thing!
   * And this is where the real magic begin!
   */
  object MoreImplicitParameters {
    //Let's call this thing a type-class!
    trait Show[-T] {
      def apply(value: T): String
    }

    /*
    This is equivalent to def show[T](value: T)(implicit show: Show[T]): String = ...
    with the difference that the implicit argument is not named and can be obtained only using 'implicitly'.

    : Show syntax is called "a context bound"
     */
    def show[T: Show](value: T): String =
      implicitly[Show[T]].apply(value)

    object syntax {
      //our old friend implicit conversion but now with an implicit value requirement
      implicit class ShowOps[T: Show](inner: T) {
        def show: String = MoreImplicitParameters.show(inner)
      }
    }

    object instances {
      /*
      Type-classes provide a way to create generic logic which can be extended to work on any type.

      Here we extend all the possible logic working on Show, to work on some standard library types.
       */

      //for String's
      implicit val stringShow: Show[String] = (value: String) => value
      //for Int's
      implicit val intShow: Show[Int] = (value: Int) => value.toString
      //even for any Seq[T] where T itself has a Show instance
      implicit def seqShow[T: Show]: Show[Seq[T]] =
        (value: Seq[T]) => value.map(show(_)).mkString("(", ", ", ")")
    }

    object Workspace {
      import instances._
      import syntax._

      /*
      And here we extend all the possible logic working on Show, to work on our custom types!
       */
      case class MyLuckyNumber(value: Int)
      object MyLuckyNumber {
        implicit val myLuckyNumberShow: Show[MyLuckyNumber] =
          (luckyNumber: MyLuckyNumber) => s"lucky ${ luckyNumber.value }"
      }

      def showEverything(): Unit = {
        println(42.show)
        println("hello!".show)
        println(Seq("I", "am", "a", "ghost").show)
        println(Seq(1, 2, 3, 4, 5).show)
        println(Seq(MyLuckyNumber(13), MyLuckyNumber(99)).show)
      }
    }
  }

  /*
  Exercise 2.

  Let us create a reverseShow method which should be defined for any T which has a Show type-class instance
   */
  object Exercise2 {
    //change the method signature accordingly
    def reverseShow(value: Any): String = ???
  }

  /*
  Exercise 3.

  There are some type-classes in Scala standard library.

  Let's get to know them better!
   */
  object Exercise3 {
    /**
     * Amount of years since the invention of the
     * hyper-drive technology (we are certainly in negative values at the moment).
     */
    case class HDEYears(value: Long)

    /*
    should be defined on any T which has Ordering[T] and return second biggest value from the sequence
    if it exists

    should work on our custom HDEYears

    change the signature accordingly, add implicit instances if needed
     */
    def secondBiggestValue[T](values: Seq[T]): Option[T] = ???


    /**
     * Custom number type!
     * For now it just wraps a Float but more interesting stuff could come in the future, who knows...
     */
    case class CustomNumber(value: Float)

    /*
    should be defined on any T which has Fractional[T], should return average value if it can be obtained

    should work on our custom CustomNumber

    change the signature accordingly, add implicit instances if needed
     */
    def average[T](values: Seq[T]): Option[T] = ???
  }

  /*
  Exercise 3.

  Let's get even more generic!
   */
  object Exercise4 {
    /*
    Generic foldLeft!

    F[_] - type constructor with a single type argument, like List[T], Option[T], etc.

    Types which are parameterized using type constructors called higher-kinded types (HKT)
    Foldable here is a HKT
     */
    trait Foldable[F[_]] {
      def foldLeft[T, S](ft: F[T], s: S)(f: (S, T) => S): S
    }

    implicit val optionFoldable: Foldable[Option] = new Foldable[Option] {
      override def foldLeft[T, S](ft: Option[T], s: S)(f: (S, T) => S): S =
        ft match {
          case None    => s
          case Some(t) => f(s, t)
        }
    }
    implicit val listFoldable: Foldable[List] = new Foldable[List] {
      override def foldLeft[T, S](ft: List[T], s: S)(f: (S, T) => S): S =
        ft.foldLeft(s)(f)
    }

    case class Triple[T](
      v1: T,
      v2: T,
      v3: T,
    )

    /*
    Part 1.

    Define an Foldable instance for Triple (should behave like a collection of 3 elements)
     */

    /*
    Part 2.

    Define another type-class - Summable[T] which should give us methods:
    - def plus(left: T, right: T): T
    - def zero: T

    Define Summable[T] instances for:
    - any T which has the standard library Numeric[T] type-class provided
    - Set[S] - zero should be Set.empty and plus should merge sets with + operation
     */

    /*
    Part 3.

    And finally - define generic collection sum method which works on any F[T]
    where F is Foldable (F[_]: Foldable) and T is Summable (T: Summable)!

    def genericSum... - work out the right method signature, should take F[T] and return T
     */
  }
}

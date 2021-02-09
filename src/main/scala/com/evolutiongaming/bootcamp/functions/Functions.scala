package com.evolutiongaming.bootcamp.functions

import java.time.Instant

object Functions {
  // Functions are expressions that have parameters, and take arguments.

  // Functions are first-class values:
  // a functions can be assigned to a value, passed as a parameter and returned as a result

  // first order functions take and return ordinary data types
  // higher order functions take and/or return other functions

  // Example.
  def clean(message: String): String = message.replaceAll("fox", "***")

  def mkUpperCase(message: String): String = message.toUpperCase

  // Pass our logic as a parameter `f`
  def processText(message: String, f: String => String): String = f.apply(message)

  def clean2(message: String): String = {
    // `s` is a parameter and may be omitted
    val f: String => String = s => s.replaceAll("fox", "***")
    processText(message, f)
  }

  def mkUpperCase2(message: String): String = {
    val f: String => String = _.toUpperCase
    processText(message, f)
  }

  // Exercise.
  // Implement `isEven` a function that checks if a number is even
  def isEven(x: Int): Boolean = ???

  // Implement `isEvenVal` which behaves exactly like `isEven`.
  // val isEvenVal: Int => Boolean = ???

  // Implement `isEvenDefToVal` by transforming `isEven` def function into a val
  // val isEvenDefToVal: Int => Boolean = ???

  // --


  // In Scala, every concrete type is a type of some class or trait
  // `(String => String)` is the same as scala.Function1[String, String]
  // `scala.Function1[A, B]` is a trait, where `A` and `B` are type parameters

  // an instance of a function can be treated as object

  // The simplified version of the scala.Function1
  object Functions {
    trait Function1[T, R] {
      // `apply` defines how we transform `T` to `R`
      def apply(v1: T): R
    }
  }

  // More common way to define a function type is just `A => B`
  // `A => B` is the type of a function that takes an arg of type A and return a result of type B

  // Syntax sugar allows to call a function w/o typing `apply`
  // `f.apply(..)` becomes `f(..)`

  // We can write a function w/o giving a name
  processText("some text", _ + "!!")

  // Anonymous function expands to implementation of scala.Function1 trait
  processText("some text", new Function1[String, String] {
    override def apply(v1: String): String = v1 + "!!"
  })

  // Method can be passed as a function, but it is not a function value, it's just converted automatically
  def trimAndWrap(v: String): String = s"<${v.trim}>"

  processText("xxx", trimAndWrap)


  // Subclassing Functions
  // One nice aspect of functions being traits is that we can subclass the function type

  trait MyMap[K, V] extends (K => V)

  // Question. What should we extend to ..

  // check if an element belongs to a set
  // > trait MySet[A] extends ???

  // return a value by its index
  // > trait MySeq[A] extends ???

  // --

  // Polymorphic functions has at least one type parameter
  // A type parameter is a form of encapsulation

  def x[T](v: T) = ???

  // Exercise.
  // Implement `mapOption` a function. Do not use scala option api
  def mapOption[A, B](option: Option[A], f: A => B): Option[B] = ???

  // --

  // Functions composition

  val strToInt: String => Int = ???
  val intToBool: Int => Boolean = ???

  val strToBool1: String => Boolean = t => intToBool(strToInt(t))
  val strToBool2: String => Boolean = intToBool.compose(strToInt)
  val strToBool3: String => Boolean = strToInt.andThen(intToBool)

  // --

  // The pattern matching block expands to the Function1 instance
  val pingPong: String => String = {
    case "ping" => "pong"
  }

  // Question. What happens next?
  // > pingPong("hi?")

  // With the function type itself we cannot find out beforehand
  // whether the function is applicable to a certain argument

  // Partial functions is another trait which extends Function and has `isDefinedAt` method

  val pingPongPF: PartialFunction[String, String] = {
    case "ping" => "pong"
  }

  pingPongPF.isDefinedAt("ping") // > true
  pingPongPF.isDefinedAt("hi") // > false


  // If expected type is a PF then a pattern matching block will expended to PF implementation

  val pingPongPFImpl: PartialFunction[String, String] = new PartialFunction[String, String] {
    override def isDefinedAt(x: String): Boolean = x match {
      case "ping" => true
      case _ => false
    }

    override def apply(v: String): String = v match {
      case "ping" => "pong"
    }
  }

  // Example of using partial functions:
  val eithers: Seq[Either[String, Double]] = List("123", "456", "789o")
    .map(x => x.toDoubleOption.toRight(s"Failed to parse $x"))

  val errors: Seq[String] = eithers.collect {
    case Left(x) => x
  }

  // We can make a function that returns another function
  // Example.
  type Language = String

  def translate(message: String, from: Language, to: Language): String = {
    // some logic
    if (from == to) message else message.reverse
  }

  val translateFromRus: (Language, String) => String =
    (to: String, message: Language) => translate(message, "rus", to)

  // `=>` has right associative law
  val translateF: Language => (Language => (String => String)) =
    (from: Language) => (to: Language) => (message: String) => translate(message, from, to)

  val fromRu = translateF("ru")
  val fromRuToEn = fromRu("en")
  val result = fromRuToEn("функция")

  // Multiple parameter lists ~ syntax sugar for functions returning a function
  def translateM(from: Language)(to: Language)(message: String): String = translate(message, from, to)

  // --

  // Pure functions are mappings between two sets

  // A function is impure if ..
  // - is not defined for all values of input type
  // - throws an exception
  // - returns a value that depends on something else than an input value
  // - works with mutable shared state
  // - does something that is not present in the function signature (side effects)
  // - relies on reflection

  // Why is Null bad?
  // null causes NullPointerException
  // null cannot be removed from the language (although Scala 3 will help handle it)
  // `null` can be passed anywhere

  // Exercise. Provide an example of an impure functions

  // Is `plus` a pure function? Why?
  def plus(a: Int, b: Int): Int = a + b

  // Is `mapLookup` a pure function? Why?
  def mapLookup(map: Map[String, Int], key: String): Int =
    map(key)

  // Pure function should:
  // - be deterministic
  // - not have side effects
  // - be total (not partial)
  // - not throw exceptions
  // - not do any mutation (local, non-local, reference, etc.)
  // - not use `null`

  // A function without side effects only returns a value


  // Exercise. Provide an example of pure functions
  // Question. If a function return for all inputs the same value, is this function pure?

  // Benefits of pure functions

  // Fearless refactoring: any value can be replaced by the function that produced it (referential transparency)
  // Documentations based on functions types
  // Easier to test: no mutation, no randomness, no side effects
  // Potential compiler optimisations
  // Make parallel processing easier


  // Exercises. Convert the following function into a pure function.
  type ??? = Nothing // just to make it compile and indicate that return type should be changed

  //
  def parseDate(s: String): Instant = Instant.parse(s)
  def parseDatePure(s: String): ??? = ???

  //
  def divide(a: Int, b: Int): Int = a / b
  def dividePure(a: Int, b: Int): ??? = ???

  //
  def isAfterNow(date: Instant): Boolean = date.isAfter(Instant.now())
  def isAfterNowPure(/* ??? */): Boolean = ???

  //
  case class Nel[T](head: T, rest: List[T])
  def nel[T](list: List[T]): Nel[T] = {
    if (list.isEmpty) println("ERROR: provide non empty list")
    Nel(list.head, list.tail)
  }
  def nelPure[T](list: List[T]): ??? = ???

  // --

  // Additional exercises:
  // https://www.scala-exercises.org/std_lib/higher_order_functions
  // https://www.scala-exercises.org/fp_in_scala/getting_started_with_functional_programming
}

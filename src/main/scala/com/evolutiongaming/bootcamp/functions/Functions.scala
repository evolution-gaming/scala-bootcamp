package com.evolutiongaming.bootcamp.functions

import java.time.Instant

object Functions {

  // FUNCTIONS

  // In mathematics, a function is a relation between two sets that associates each element of the first set
  // to exactly one element of the second set. In programming, a function is a block of code that accomplishes
  // a specific task. A function usually takes in data (a list of arguments), processes it and returns a
  // result (eventually).
  //
  // In Scala functions are first-class citizens. They can be assigned to a value, passed as a parameter and
  // returned as a result.

  // FIRST- AND HIGHER-ORDER FUNCTIONS

  // First-order functions take and return only ordinary data types.
  //
  // Higher-order functions take and/or return other functions.

  // Question. Are these first-order of higher order functions?
  val normalize: String => String                       = message => message.trim.toLowerCase
  val processText: (String, String => String) => String = (message, f) => f.apply(message)

  // Scala has both functions and methods. Most of the time we can ignore this distinction (as done in this
  // lecture), however, internally they are two different things. Scala method, as in Java, is a part of a
  // class. It has a name, a signature, optionally some annotations. Scala function is a complete object,
  // which has its own methods: `apply`, `compose`, `andThen`, `curried`, `tupled`, etc.
  def normalize2(message: String): String = message.trim.toLowerCase

  // Syntax sugar allows calling a function without typing `apply`: `f.apply()` becomes `f()`.
  def processText2(message: String, f: String => String): String = f(message)

  // Exercise. Implement `isEven` method that checks if a number is even.
  def isEven(n: Int): Boolean = ???

  // Exercise. Implement `isEvenFunc` function that behaves exactly like `isEven` method.
  val isEvenFunc: Int => Boolean = n => ???

  // Exercise. Implement `isEvenMethodToFunc` function by transforming `isEven` method into a function.
  val isEvenMethodToFunc: Int => Boolean = n => ???

  // There are traits in Scala to represent functions with various numbers of arguments: `Function0`,
  // `Function1`, `Function2`, etc. So `(A => B)` is the same as `Function1[A, B]`. A trait, where
  // `A` and `B` are type parameters. Here is how simplified version of `Function1[A, B]` looks like.
  object Functions {
    trait Function1[A, B] {

      /** Defines how to transform `A` to `B`. */
      def apply(a: A): B
    }
  }

  // We can write anonymous functions (functions without explicit names).
  processText("Hello, world", _ + "!")

  // Anonymous functions also expand to the implementation of the `Function1[A, B]` trait.
  processText(
    "Hello, world",
    new Function1[String, String] {
      override def apply(a: String): String = a + "!"
    },
  )

  // Methods can be passed where functions are required, in such cases Scala automatically converts them.
  def trimAndWrap(v: String): String = s"<${v.trim}>"
  processText(" abc ", trimAndWrap)

  // One interesting aspect of functions being traits is that we can subclass function types.
  trait MyMap[K, V] extends (K => V)

  // Question. What function should we extend to check if an element belongs to a set?
  trait MySet[A] // extends ???

  // Question. What function should we extend to return a value by its index?
  trait MySeq[A] // extends ???

  // POLYMORPHIC FUNCTIONS

  // Polymorphic functions have at least one type parameter.

  // Exercise. Implement `mapOption` function without calling `Option` APIs.
  def mapOption[A, B](option: Option[A], f: A => B): Option[B] = ???

  // FUNCTION COMPOSITION

  val strToInt: String => Int   = _.length
  val intToBool: Int => Boolean = _ > 10

  // Function traits provide handy methods to compose multiple functions into one.
  val strToBool1: String => Boolean = t => intToBool(strToInt(t))
  val strToBool2: String => Boolean = intToBool.compose(strToInt)
  val strToBool3: String => Boolean = strToInt.andThen(intToBool)

  // PARTIAL FUNCTIONS

  // Pattern matching blocks expand to `Function1` instances.
  val pingPong: String => String = { case "ping" =>
    "pong"
  }

  // Question. What happens next?
  // pingPong("hi")

  // With standard functions we cannot find out beforehand whether the function is applicable to a certain
  // argument or not. `PartialFunction` extends `Function` and helps to solve this via `isDefinedAt` method.
  val pingPongPF: PartialFunction[String, String] = { case "ping" =>
    "pong"
  }
  pingPongPF.isDefinedAt("ping") // true
  pingPongPF.isDefinedAt("hi") // false

  // CURRYING

  type Language = String

  // In Scala, if a function accepts multiple parameters, by default they can only be supplied all at once.
  val translate: (Language, Language, String) => String =
    (from, to, text) => if (from == to) text else text.reverse
  val translateResult: String                           = translate("en", "lv", "Hello, world!")

  // However, sometimes it makes more sense to supply arguments one by one. Curring helps to achieve that.
  // It transforms a function that takes multiple arguments into a function that takes a single argument
  // and returns back another function. Currying can be done manually...
  val translateCurried: Language => (Language => (String => String)) = {
    from => (to => (text => translate(text, from, to)))
  }

  // ... or by calling `curried` method.
  val translateCurried2: Language => Language => String => String = translate.curried

  val translateEn: Language => String => String = translateCurried("en")
  val translateEnToLv: String => String         = translateEn("lv")
  val translateResult2: String                  = translateEnToLv("Hello, world!")

  val translateResult3: String = translateCurried("en")("lv")("Hello, world!")
  val translateResult4: String = translate.curried("en")("lv")("Hello, world!")

  // PURE FUNCTIONS

  // Pure function is a computational analogue of a mathematical function.
  //
  // A function is pure if it:
  // 1. returns values that are identical for identical arguments;
  // 2. has no side effects.
  //
  // Examples of side effects:
  // - throwing exceptions;
  // - mutating state;
  // - writing to I/O streams;
  // - depending on the current date or time;
  // ...
  //
  // Pure functions are referentially transparent. This means they can be replaced with their corresponding
  // return values (and vice-versa) without changing the program's behavior.

  // A function is impure if it:
  // - is not defined for all values (is partial);
  // - throws exceptions;
  // - returns a value that depends on something else other than input arguments;
  // - works with shared mutable state;
  // ...

  // Question. Why usage of `null` breaks function purity?

  // Question. Is `plus` a pure function? Why?
  def plus(a: Int, b: Int): Int = a + b

  // Question. Is `mapLookup` a pure function? Why?
  def mapLookup(map: Map[String, Int], key: String): Int = map(key)

  // Question. If a function returns the same value for all inputs, is it pure?

  // Building programs with pure functions has the following benefits:
  // - Fearless refactoring. Any function call can be replaced with its return value.
  // - Type safety. Better compile-time error reporting. No unexpected exceptions at runtime.
  // - Improved testability. No mutation. No randomness. No side effects.

  type ??? = Nothing

  // Exercises. Convert the following functions into pure functions. Replace ??? with correct return types.

  def parseDate(s: String): Instant = Instant.parse(s)
  def parseDatePure(s: String): ??? = ???

  def divide(a: Int, b: Int): Int     = a / b
  def dividePure(a: Int, b: Int): ??? = ???

  def isAfterNow(date: Instant): Boolean   = date.isAfter(Instant.now())
  def isAfterNowPure( /* ??? */ ): Boolean = ???

  case class NonEmptyList[T](head: T, rest: List[T])
  def makeNonEmptyList[T](list: List[T]): NonEmptyList[T] = {
    if (list.isEmpty) println("Error: list must not be empty")
    NonEmptyList(list.head, list.tail)
  }
  def makeNonEmptyListPure[T](list: List[T]): ???         = ???

  // Attributions and useful links:
  // https://jim-mcbeath.blogspot.com/2009/05/scala-functions-vs-methods.html
  // https://www.scala-exercises.org/std_lib/higher_order_functions
  // https://www.scala-exercises.org/fp_in_scala/getting_started_with_functional_programming
}

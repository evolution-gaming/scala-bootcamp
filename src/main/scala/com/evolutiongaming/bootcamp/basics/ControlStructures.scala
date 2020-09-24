package com.evolutiongaming.bootcamp.basics

import java.io.FileNotFoundException

import scala.annotation.tailrec
import scala.io.Source
import scala.util.{Failure, Success, Try}

object ControlStructures {
  // You can follow your progress using the tests in `ControlStructuresSpec`.
  //   sbt "testOnly com.evolutiongaming.bootcamp.basics.ControlStructuresSpec"

  // The if-then-else construct is as follows:
  //
  // val result =
  //   if (boolean1) {
  //     result1
  //   } else if (boolean2) {
  //     result2
  //   } else {
  //     otherResult
  //   }
  //
  // Note that it returns a result value.

  // Note that curly braces can be omitted
  //  val result =
  //    if (boolean1) result1
  //    else if (boolean2) result2
  //    else otherResult

  // Exercise. Implement a "Fizz-Buzz" https://en.wikipedia.org/wiki/Fizz_buzz function using the if-then-else,
  // returning "fizzbuzz" for numbers which divide with 15, "fizz" for those which divide by 3 and "buzz" for
  // those which divide with 5, and returning the input number as a string for other numbers:
  def fizzBuzz1(n: Int): String = ???

  // Pattern Matching
  //
  // Using the match-case construct we can write constructs equivalent to if-then-else statements in, often,
  // a more readable and concise form:
  //
  // val result = someValue match {
  //    case pattern1                       => result1
  //    case pattern2 if (guardCondition)   => result2
  //    case _                              => fallbackResult
  // }

  type ErrorMessage = String
  def monthName(x: Int): Either[ErrorMessage, String] = {
    x match {
      case 1            => Right("January")
      case 2            => Right("February")
      case 3            => Right("March")
      case 4            => Right("April")
      case 5            => Right("May")
      case 6            => Right("June")
      case 7            => Right("July")
      case 8            => Right("August")
      case 9            => Right("September")
      case 10           => Right("October")
      case 11           => Right("November")
      case 12           => Right("December")
      case x if x <= 0  => Left(s"Month $x is too small")
      case x            => Left(s"Month $x is too large")
    }
  }

  // Question. How would you improve `monthName`?
  // Question. What would you use in its place if you wanted to more properly handle multiple locales?

  // Exercise. Implement a "Fizz-Buzz" function using pattern matching:
  def fizzBuzz2(n: Int): String = ???

  // Recursion
  //
  // A function which calls itself is called a recursive function. This is a commonly used way how to
  // express looping constructs in Functional Programming languages.

  def sum1(list: List[Int]): Int = {
    if (list.isEmpty) 0
    else list.head + sum1(list.tail)
  }

  // Question. What are the risks of recursion when applied without sufficient foresight?

  // @tailrec annotation verifies that a method will be compiled with tail call optimisation.
  @tailrec
  def last[A](list: List[A]): Option[A] = list match {
    case Nil        => None
    case x :: Nil   => Some(x)
    case _ :: xs    => last(xs)
  }

  // In reality, recursion isn't used that often as it can be replaced with `foldLeft`, `foldRight`,
  // `reduce` or other larger building blocks.

  def sum2(list: List[Int]): Int = {
    list.foldLeft(0)((acc, x) => acc + x)
  }

  def sum3(list: List[Int]): Int = {
    list.foldRight(0)((x, acc) => acc + x)
  }

  def sum4(list: List[Int]): Int = {
    if (list.isEmpty) 0
    else list.reduce((a, b) => a + b)
  }

  def sum5(list: List[Int]): Int = {
    list.sum // only for Numeric lists
  }

  // Exercise: Implement a function `applyNTimes` which takes a function `f` and an integer `n` and
  // returns a function which applies the function `f` `n` times.
  //
  // Thus `applyNTimesForInts(_ + 1, 4)(3)` should return `((((3 + 1) + 1) + 1) + 1)` or `7`.
  def applyNTimesForInts(f: Int => Int, n: Int): Int => Int = { x: Int =>
    f(x + n) // replace with a correct implementation
  }

  // Exercise: Convert the function `applyNTimesForInts` into a polymorphic function `applyNTimes`:
  def applyNTimes[A](f: A => A, n: Int): A => A = { x: A =>
    // replace with correct implementation
    println(n)
    f(x)
  }

  // `map`, `flatMap` and `filter` are not control structures, but methods that various collections (and
  // not only collections) have. We will discuss them now as they are important to understand a key
  // control structure called "for comprehensions".

  // `map` is a higher order function which - in case of collections - transforms each element of the
  // collection into a different element (and returns the resulting collection)

  // For example, for `List` it is defined as
  object list_map_example { // name-spacing to not break other code in this lesson
    class List[A] {
      def map[B](f: A => B): List[B] = ???
    }
  }

  // Question. What is the value of this code?
  val listMapExample = List(1, 2, 3).map(x => x * 2)

  // As we will see in later lessons, `map` is a method that `Functor`-s have, and there are more `Functor`-s
  // than just collections (`IO`, `Future`, `Either`, `Option` are all `Functor`-s too).

  // For now, we will have a utilitarian focus and not go into `Functor`-s and other type classes.

  // `flatMap` is a higher order function which - for collections - transforms each element of the collection
  // into a collection, and then `flatten`-s these collections.

  // For example, for `List` it could be defined as:
  object list_flatmap_example { // name-spacing to not break other code in this lesson
    class List[A] {
      def flatMap[B](f: A => List[B]): List[B] = ???
    }
  }

  // Question. What is the value of this code?
  val listFlatMapExample = List(1, 2, 3).flatMap(x => List(x, x * 2))

  // Question. Do you think only collections can have `flatMap`?

  // `filter` takes a predicate function returning a boolean and - for collections - returns a collection
  // with only these elements which satisfy this predicate.

  // For example, for `List` it is defined as:
  object list_filter_example {
    class List[A] {
      def filter(p: A => Boolean): List[A] = ???
    }
  }

  // Question. What is the value of this code?
  val listFilterExample = List(1, 2, 3).filter(_ % 2 == 0)

  // For Comprehensions

  // A `for-yield` syntax is syntactic sugar for composing multiple `map`, `flatMap` and `filter` operations
  // together in a more readable form.

  // val result = for {
  //   x <- a
  //   y <- b
  // } yield x + y
  //
  // gets translated to
  //
  // val result = a.flatMap(x => b.map(y => x + y))

  private val a = List(1, 2, 3)
  private val b = List(10, 100)

  val c = for {
    x <- a
    y <- b
  } yield x * y

  val d = a.flatMap(x => b.map(y => x * y))

  // Question: What is the value of `c` above?
  // Question: What is the value of `d` above?

  // You can also add `if` guards to `for` comprehensions:
  val e = for {
    x       <- a
    if x % 2 == 1
    y       <- b
  } yield x + y

  // Question. What is the value of `e` above?

  // In idiomatic functional Scala, much of the code ends up written in "for comprehensions".
  // Exercise. Implement `makeTransfer` using `for` comprehensions and the methods provided in `UserService`.

  type UserId = String
  type Amount = BigDecimal

  trait UserService {
    def validateUserName(name: String): Either[ErrorMessage, Unit]
    def findUserId(name: String): Either[ErrorMessage, UserId]
    def validateAmount(amount: Amount): Either[ErrorMessage, Unit]
    def findBalance(userId: UserId): Either[ErrorMessage, Amount]

    /** Upon success, returns the resulting balance */
    def updateAccount(userId: UserId, previousBalance: Amount, delta: Amount): Either[ErrorMessage, Amount]
  }

  // Upon success, should return the remaining amounts on both accounts (fromUser, toUser).
  def makeTransfer(service: UserService, fromUser: String, toUser: String, amount: Amount): Either[ErrorMessage, (Amount, Amount)] = {
    // Replace with a proper implementation:
    println(s"$service, $fromUser, $toUser, $amount")
    ???
  }

  // Question. What are the questions would you ask - especially about requirements - before implementing
  // this function? What potential issues can you run into?

  // Exercise. The tests for `makeTransfer` in `ControlStructuresSpec` are insufficient. Improve the tests.

  // Question. Does the implementation of `makeTransfer` depend on `Either` being the "container"?

  // Let us return to our "intuition about types" exercises from before.

  // Exercise:
  //
  // Given:
  //  A = Set(0, 1, 2)
  //  B = Set(true, false)
  //
  // List all the elements in `A * B`.
  //
  // Use a "for comprehension" in your solution.

  val AProductB: Set[(Int, Boolean)] = Set()

  // Exercise:
  //
  // Given:
  // A = { 0, 1, 2 }
  // B = { true, false }
  //
  // List all the elements in `A + B`.
  //
  // Use "map" and `++` (`Set` union operation) in your solution.

  val ASumB: Set[Either[Int, Boolean]] = Set()

  // Scala inherits the standard try-catch-finally construct from Java:
  def printFile(fileName: String): Unit = {
    // This code is only here to illustrate try-catch-finally, it shouldn't be considered as good code
    val source = Source.fromFile(fileName)
    try { // executed until an exception happens
      source.getLines() foreach println
    } catch { // exception handlers
      case e: FileNotFoundException   => println(s"Couldn't find the file: $e")
      case e: Exception               => println(s"Exception occurred: $e")
    } finally { // executed even if an exception happens
      source.close
    }
  }

  // Question. What issues can you find with the above `printFile` method?

  // However, in idiomatic functional Scala, other error handling mechanisms are usually used.
  // Throwing exceptions is an anti-pattern - it introduces another exit path from a function and breaks
  // referential transparency.

  // One of them is `Try` which can be thought of as an `Either[Throwable, A]`:

  def parseInt1(x: String): Try[Int] = Try(x.toInt)

  parseInt1("asdf") match {
    case Success(value) => println(value)
    case Failure(error) => println(error)
  }

  // Question. What other ways of representing the "parse string to integer" results can you think of?
}

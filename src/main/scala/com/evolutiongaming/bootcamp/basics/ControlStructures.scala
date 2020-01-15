package com.evolutiongaming.bootcamp.basics

import scala.annotation.tailrec

object ControlStructures {
  // You can follow your progress using the tests in `ControlStructuresSpec`.

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

  // Question. How is List.sum implemented in the standard library?

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

  // TODO: map, flatMap, for comprehensions
  // TODO: exceptions, try-finally & Try

  // Exercise:
  //
  // Given:
  //  A = Set(0, 1, 2)
  //  B = Set(true, false)
  //
  // List all the elements in `A * B`.
  //
  val AProductB: Set[(Int, Boolean)] = Set()

  // Exercise:
  //
  // Given:
  // A = { 0, 1, 2 }
  // B = { true, false }
  //
  // List all the elements in `A + B`.
  //
  val ASumB: Set[Either[Int, Boolean]] = Set()
}

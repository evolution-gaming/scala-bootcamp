package com.evolutiongaming.bootcamp.state.v3

import scala.collection.mutable.ArrayBuffer

/** 'Shared State in FP'
  *
  *  Our goals:
  *  * understand the terminology that helps to reason about pure/impure state (RT, purity, side effects)
  *  * look at how the imperative state looks like and find a way to represent it in a purely functional way
  *  * make the state thread-safe so that we can use it in concurrent programs
  *  * get familiar with functional primitives and understand basic building blocks of concurrent state
  *  * introduce STM to work with in-memory transactions
  */

/*
- referential transparency
  https://impurepics.com/tags/rt/page/1.html
 * expression vs statement
 * expression: combination of values and operators that results in a single value
 * statement: if/else, val = 0, println("s"), for/while

 * what's a side effect?
 * when function uses something outside its parameters e.g. mutating shared state, I/O
 * side effects are causing the program to behave unexpectedly

 * what's a pure function?
 * function that doesn't perform side effects

 * what's referential transparency (RT)?
 * expression is referentially transparent if it can be replaced by a value it produces without changing meaning of a program

- the motivation behind RT
 * local reasoning
 * functional composition
 * testability

- the examples:
 * local mutable state
 * shared mutable state
 * shared immutable state
 */

object SideEffect extends App {
  val y = {
    println("x")
    42
  } + {
    println("x")
    42
  }

  // the above can't be replaced with:
  val y1 = 42 + 42
  // without changing meaning of a program
}

object ReferentialTransparency extends App {
  // 1. functions 'reverse' and '++' are pure
  // 2. we can use substitution model to reason about composition of pure functions
  val x  = "hello".reverse
  val y  = x ++ x
  val y1 = "hello".reverse ++ "hello".reverse

  println(s"y=$y, y1=$y1")
}

object MutableSharedState extends App {
  // mutable shared state is not RT
  val map = scala.collection.mutable.Map("foo" -> 5, "bar" -> 7)
  val foo = map("foo")
  val bar = map("bar")
  val x   = foo + bar

  map.addOne("foo" -> 6) // mutating shared state is a side effect
  val y = map("foo") + map("bar")

  println(s"x=$x, y=$y")
}

object ImmutableSharedState extends App {
  // immutable shared state is RT
  val map = scala.collection.immutable.Map("foo" -> 5, "bar" -> 7)
  val foo = map("foo")
  val bar = map("bar")
  val x   = foo + bar

  val map1 =
    map + ("foo" -> 6) // this returns a new reference instead of mutating existing one
  val y = map("foo") + map("bar")

  println(s"x=$x, y=$y")
}

/** Does using mutable state always mean that resulting code will NOT be referentially transparent?
  * It's not always the case. But as a rule of thumb immutable state is 'safer'.
  */
object MutableLocalState extends App {
  // mutable local state is RT
  def immutableMap[A, B](list: List[A])(f: A => B): List[B] = list match {
    case head :: tail => f(head) +: immutableMap(tail)(f)
    case Nil          => Nil
  }

  // mutable local state can be RT from the 'outside'
  def mutableMap[A, B](list: List[A])(f: A => B): List[B] = {
    val res = new ArrayBuffer[B]()
    for (l <- list)
      res.addOne(f(l))
    res.toList
  }

  def duplicate(s: String) = s ++ s

  val list = "foo" :: "bar" :: Nil

  println(
    s"immutable: ${immutableMap(list)(duplicate)}, mutable: ${mutableMap(list)(duplicate)}"
  )
}

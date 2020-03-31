package com.evolutiongaming.bootcamp.typeclass

import cats.kernel.Monoid

import scala.concurrent.ExecutionContext
import scala.language.implicitConversions

class SomeImplicits {

  // Implicit Conversion
  implicit def anyName(int: Int): String = int.toString
//  implicit val anyName: Int => String = _.toString

  // - convert to expected type
  def putLn(s: String): Unit = println(s)
  putLn(123) // 123 is being converted to string

  // - find a method on expected type
  123.contains("12")

  // Sugared implicit conversion
  implicit class RichString(s: String) {
    def myFancyMethod(k: Int): Set[String] = s.grouped(k).toSet
  }



  // Implicit Parameters
  def printEc()(implicit ec: ExecutionContext): Unit = {
    println(ec)
  }

  import ExecutionContext.Implicits.global
  // or implicit val global = ExecutionContext.global
  printEc()
  implicitly[ExecutionContext] // get implicit

  def sum[T](list: List[T])(implicit integral: Monoid[T]): T = {
    import cats.instances.all._
    import cats.syntax.all._
    list.combineAll
  }



  // Context Bounds
  def sugaredSum[T: Monoid](list: List[T]): T = {
    import cats.instances.all._
    import cats.syntax.all._
    list.combineAll
  }


}

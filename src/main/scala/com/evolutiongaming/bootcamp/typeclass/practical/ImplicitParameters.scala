package com.evolutiongaming.bootcamp.typeclass.practical

import scala.util.Random

object ImplicitParameters extends App {

  implicit val name = "Oleg"

  LuckService.greet(name)
  LuckService.predictLuck(name)
  LuckService.bye(name)
}

object LuckService {
  def greet(name: String): Unit = println(s"Hello $name")
  def predictLuck(name: String): Unit = println(s"Your luck is ${Random.nextInt(11)} today, $name")
  def bye(name: String): Unit = println(s"See you $name")
}

object ImplicitConversions extends App {

  implicit val nameDoesntMatter: ((Int, Int)) => Point = {
    case (x, y) => Point(x, y)
  }

  implicit val nameDoesntMatter2: Int => Point = {
    x => Point(x, x)
  }

  println(Point(4, 5).double)

  println((4, 5).double)
  println(4.double)


  case class Point(x: Int, y: Int) {
    def double: Point = Point(2 * x, 2 * y)
  }
}

object ImplicitConversionsForSyntax extends App {

  // stupid class used only for adding a new method to an existing type
  case class MyTupleExt(t: (Int, Int)) {
    def double: (Int, Int) = {
      val (x, y) = t
      (2 * x, 2 * y)
    }
  }

  // an implicit conversion
  implicit val nameDoesntMatter: ((Int, Int)) => MyTupleExt = MyTupleExt

  // nobody expects MyTupleExt as param so it is used only for adding new methods
  (1, 2).double
}

object ImplicitConversionsSugar extends App {

  println((4, 5).double)

  implicit class TupleExt(x: (Int, Int)) {
    def double: (Int, Int) = (2 * x._1, 2 * x._2)
  }

  def maxDepth(s: String): Int = {
    var result: Int = 0
    s.foldLeft(0) { (acc, ch) =>
      if (ch == '(') acc + 1
      else if (acc > result) {
        result = acc
        acc
      } else if (ch == ')') acc - 1
      else
        acc
    }
    result
  }
}

object Task {
  // write an implicit class so the next line compiles
  // val b: Boolean = List(true, true).allTrue
}


package com.evolutiongaming.bootcamp.typeclass.practical

import scala.util.Random

object ImplicitParameters extends App {

  implicit val name = "Oleg"

  LuckService.greet
  LuckService.predictLuck
  LuckService.bye("Olga")
}

object LuckService {
  def greet(implicit name: String): Unit = println(s"Hello $name")
  def predictLuck(implicit name: String): Unit = println(s"Your luck is ${Random.nextInt(11)} today, $name")
  def bye(implicit name: String): Unit = println(s"See you $name")
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


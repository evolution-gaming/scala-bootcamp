package com.evolutiongaming.bootcamp.typeclass.v2


object BasicImplicitConversion extends App {
  case class A(x: Int)
  case class B(x: Int)

  implicit def conversion(a: A): B = B(a.x)

  def myMethod(b: B): Unit = println(b)

  myMethod(A(123))
}

object ImplicitConversionChaining extends App {
  case class A(x: Int)
  case class B(x: Int)
  case class C(x: Int)

  implicit def conversion(a: A): B = B(a.x)
  implicit def conversion2(b: B): C = C(b.x)

  def myMethod(c: C): Unit = println(c)

  // you can't do this! implicits don't chain
  // myMethod(A(123))
}

object ImplicitConversionExample extends App {

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

object ImplicitConversionForSyntax extends App {

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

}

object Task {
  // write an implicit class so the next line compiles
  // val b: Boolean = List(true, true).allTrue
}

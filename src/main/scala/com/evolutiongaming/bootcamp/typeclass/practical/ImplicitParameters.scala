package com.evolutiongaming.bootcamp.typeclass.practical

import scala.util.Random

// Implicits: implicit parameters
//            implicit conversions

object ImplicitParameters extends App {

  val name = "Oleg"

  LuckService.greet(name)
  LuckService.predictLuck(name)
  LuckService.bye(name)
}

object LuckService {
  def greet(name: String): Unit = println(s"Hello $name")
  def predictLuck(name: String): Unit = println(s"Your luck is ${Random.nextInt(11)} today, $name")
  def bye(name: String): Unit = println(s"See you $name")
}

object ImplicitParamTask {

  object Task1 {

    final case class User(id: String)

    trait DbConnection

    object DbConnection {
      def apply(): DbConnection = new DbConnection {}
    }

    // make second argument implicit
    def createUser(user: User, connection: DbConnection): Unit = ???
    createUser(User("123"), DbConnection())
  }

  object Task2 {
    final case class Money(amount: Int)
    val list: List[Money] = ???
//    oh no, i won't compile
//    list.sorted
  }
}

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


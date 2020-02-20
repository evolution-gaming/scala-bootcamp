package com.evolutiongaming.bootcamp.typeclass

import scala.util.{Random, Try}


object Manual {
  //  https://scalac.io/typeclasses-in-scala/


  // trait with functionality
  // an instance
  // use an instance
  // context bound
  // syntax
  // use syntax

  // typeclass
  trait Semigroup[A] {
    def append(x: A, y: A): A
  }

  object Semigroup {
    // instance for string
    implicit val stringSemigroup = new Semigroup[String] {
      override def append(x: String, y: String): String = x ++ y
    }
  }

  // usage
  def combine1[T](list: List[T])(implicit semigroup: Semigroup[T]): Option[T] = {
    Some(list)
      .filter(_.nonEmpty)
      .map(_.reduce(semigroup.append))
  }

  // sugared usage
  def combine2[T: Semigroup](list: List[T]): Option[T] = {
    Some(list)
      .filter(_.nonEmpty)
      .map(_.reduce(implicitly[Semigroup[T]].append))
  }

  // syntax
  implicit class SemigroupOps[A: Semigroup](a: A) {
    def |+|(b: A): A = implicitly[Semigroup[A]].append(a, b)
  }

  // sugared-sugared usage
  def combine3[T: Semigroup](list: List[T]): Option[T] = {
    Some(list)
      .filter(_.nonEmpty)
      .map(_.reduce(_ |+| _)) //  using syntax here
  }


  // apply
  // syntax anyval
  // instance method
  // SAM
  // instances out of companion

}

//
//object Generated {
//  import simulacrum._
//
//  @typeclass trait Semigroup[A] {
//    def append(x: A, y: A): A
//  }
//
//  def combine[T: Semigroup](list: List[T]): Option[T] = {
//    Some(list)
//      .filter(_.nonEmpty)
//      .map(_.reduce(Semigroup[T].append)) // using syntax
//  }
//
//}

/*
  Task: implement your own typeclass for example:
    - equals
    - show
    - ordering
 */


object DuelTypeClass extends App {

  trait Duel[T] {
    def duel(first: T, second: T): T
  }

  object Duel {

    implicit class DuelOps[A](val first: A) extends AnyVal {
      def $_fight_$(second: A)(implicit duel: Duel[A]): A = duel.duel(first, second)
    }

    implicit val intDuel: Duel[Int] = (first, second) => {
      val random = Random.between(first.min(second), first.max(second) + 1)
      if (math.abs(second - random) <  math.abs(first - random)) second else first
    }

    implicit val stringDuel: Duel[String] = (first, second) => {
      def res: String = {
        val firstInt = first.map(_.toInt).sum / first.length
        val secInt = second.map(_.toInt).sum / second.length
        if ((firstInt $_fight_$ secInt) == firstInt) first else second
      }
      Try(res).getOrElse(first)
    }
  }

  import Duel._

  println(s"${12 $_fight_$ 32} wins!")
  println(("ivan" $_fight_$ "vrag_ivana") + " wins!")
  println(("" $_fight_$ "") + " wins!")
  println(5 $_fight_$ 5)
}
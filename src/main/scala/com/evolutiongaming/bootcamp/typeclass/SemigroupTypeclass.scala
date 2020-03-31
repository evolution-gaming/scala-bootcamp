package com.evolutiongaming.bootcamp.typeclass

import scala.language.implicitConversions

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


object Generated {
  import simulacrum._

  @typeclass trait Semigroup[A] {
    def append(x: A, y: A): A
  }

  def combine[T: Semigroup](list: List[T]): Option[T] = {
    Some(list)
      .filter(_.nonEmpty)
      .map(_.reduce(Semigroup[T].append)) // using syntax
  }

}

/*
  Task: implement your own typeclass for example:
    - equals
    - show
    - ordering
 */

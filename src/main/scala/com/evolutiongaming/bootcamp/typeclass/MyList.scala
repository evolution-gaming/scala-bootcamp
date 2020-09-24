package com.evolutiongaming.bootcamp.typeclass

object MyList {

  sealed abstract class List[+A]

  final case class Cons[A](head: A, next: List[A]) extends List[A]

  final case object Nil extends List[Nothing]

  Cons(1, Cons(2, Cons(3, Nil)))
}

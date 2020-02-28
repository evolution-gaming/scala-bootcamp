package com.evolutiongaming.bootcamp.variance

import MyList._

sealed trait MyList[+A] {

  def map[B](f: A => B): MyList[B] = this match {
    case Nil              => Nil
    case Cons(head, tail) => Cons(f(head), tail.map(f))
  }

  final def foldl[B](init: B)(f: (A, B) => B): B = this match {
    case Nil              => init
    case Cons(head, tail) => tail.foldl(f(head, init))(f)
  }

  def prepend[B >: A](value: B): MyList[B] = Cons(value, this)

  def sum(implicit ev: A <:< Int): Int = foldl(0)(_ + _)
}

object MyList {

  def empty[A]: MyList[A] = Nil

  def apply[A](head: A, other: A*): MyList[A] = {
    def rec(other: Seq[A]): MyList[A] = other match {
      case head +: tail => Cons(head, rec(tail))
      case _            => Nil
    }
    Cons(head, rec(other))
  }

  case object Nil extends MyList[Nothing]
  final case class Cons[A](head: A, tail: MyList[A]) extends MyList[A]
}
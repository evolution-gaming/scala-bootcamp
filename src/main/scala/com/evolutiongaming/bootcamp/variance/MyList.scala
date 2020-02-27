package com.evolutiongaming.bootcamp.variance

import MyList._

sealed trait MyList[A] {

  def map[B](f: A => B): MyList[B] = ???

  final def foldl[B](init: B)(f: (A, B) => B): B = ???

//  def prepend[B](value: B): MyList[B] = ???
//
//  def sum[B]: Int = ???
}

object MyList {

  def empty[A]: MyList[A] = ???

  def apply[A](head: A, other: A*): MyList[A] = ???

  case object Nil extends MyList[Nothing]
  final case class Cons[A](head: A, tail: MyList[A]) extends MyList[A]
}
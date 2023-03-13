package com.evolutiongaming.bootcamp.collections

sealed trait CherryTree[+A] {
  import CherryTree.{Empty, One, Many}
  def prepend[A1 >: A](a: A1): CherryTree[A1] = this match {
    case Empty                        => One(a)
    case One(b)                       => Many(Single(a), Empty, Single(b))
    case Many(Single(b), mid, after)  => Many(Pair(a, b), mid, after)
    case Many(p: Pair[A], mid, after) => Many(Single(a), mid.prepend(p), after)
  }

  def append[A1 >: A](a: A1): CherryTree[A1] = this match {
    case Empty                         => One(a)
    case One(b)                        => Many(Single(b), Empty, Single(a))
    case Many(before, mid, Single(b))  => Many(before, mid, Pair(b, a))
    case Many(before, mid, p: Pair[A]) => Many(before, mid.append(p), Single(a))
  }

  def get(i: Int, size: Int): Option[A] = this match {
    case Empty => None
    case One(a) =>
      if (i == 0) Some(a)
      else None
    case Many(before, mid, after) =>
      if (i < before.size) before match {
        case Single(first)       => Some(first)
        case Pair(first, second) => if (i == 0) Some(first) else Some(second)
      }
      else if (i >= size - after.size) after match {
        case Single(first)       => Some(first)
        case Pair(first, second) => if (i == size - 1) Some(second) else Some(first)
      }
      else
        mid.get(i - before.size, size - before.size - after.size).map { case Pair(a, b) =>
          if ((i - before.size) % 2 == 0) a else b
        }
  }
}

object CherryTree {
  case object Empty extends CherryTree[Nothing]
  final case class One[+A](value: A) extends CherryTree[A]
  final case class Many[+A](
      before: Peduncle[A],
      mid: CherryTree[Pair[A]],
      after: Peduncle[A]
  ) extends CherryTree[A]
}

sealed trait Peduncle[+A] {
  def first: A
  def size: Int
}
case class Single[+A](first: A) extends Peduncle[A] {
  def size: Int = 1
}
case class Pair[+A](first: A, second: A) extends Peduncle[A] {
  def size: Int = 2
}


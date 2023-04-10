package com.evolutiongaming.bootcamp.collections

import scala.collection.IndexedSeqOps
import scala.collection.IterableFactoryDefaults
import scala.collection.SeqFactory
import scala.collection.mutable.Builder
import scala.collection.mutable.ReusableBuilder
import scala.collection.StrictOptimizedSeqFactory

sealed trait CherryTree[+A]
    extends IndexedSeq[A]
    with IterableFactoryDefaults[A, CherryTree]
    with IndexedSeqOps[A, CherryTree, CherryTree[A]] {
  import CherryTree.{Empty, One, Many}

  override def prepended[A1 >: A](a: A1): CherryTree[A1] = this match {
    case Empty                              => One(a)
    case One(b)                             => Many(Single(a), Empty, Single(b), 2)
    case Many(Single(b), mid, after, size)  => Many(Pair(a, b), mid, after, size + 1)
    case Many(p: Pair[A], mid, after, size) => Many(Single(a), mid.prepended(p), after, size + 1)
  }

  override def appended[A1 >: A](a: A1): CherryTree[A1] = this match {
    case Empty                               => One(a)
    case One(b)                              => Many(Single(b), Empty, Single(a), 2)
    case Many(before, mid, Single(b), size)  => Many(before, mid, Pair(b, a), size + 1)
    case Many(before, mid, p: Pair[A], size) => Many(before, mid.appended(p), Single(a), size + 1)
  }

  def getOption(i: Int): Option[A] = this match {
    case Empty                          => None
    case One(a)                         =>
      if (i == 0) Some(a)
      else None
    case Many(before, mid, after, size) =>
      if (i < before.size) before.getOption(i)
      else if (i >= size - after.size) after.getOption(i - size + after.size)
      else
        mid.getOption((i - before.size) / 2).flatMap(_.getOption((i - before.size) % 2))
  }

  def updatedWith[A1 >: A](f: A => A1)(index: Int): CherryTree[A1] = this match {
    case Empty      => Empty
    case One(a)     => if (index == 0) One(f(a)) else this
    case m: Many[A] =>
      if (index < m.before.size) m.copy(before = m.before.updatedWith(f)(index))
      else if (index >= size - m.after.size) m.copy(after = m.after.updatedWith(f)(index - size + m.after.size))
      else m.copy(mid = m.mid.updatedWith(_.updatedWith(f)((index - m.before.size) % 2))((index - m.before.size) / 2))
  }

  override def updated[B >: A](index: Int, elem: B): CherryTree[B] = updatedWith(_ => elem)(index)

  override def drop(n: Int): CherryTree[A] =
    if (n <= 0) this
    else if (n >= length) Empty
    else if (n == length - 1) One(last)
    else
      this match {
        case Empty | One(_)                               => Empty
        case Many(Pair(_, b), mid, after, size) if n == 1 => Many(Single(b), mid, after, size - 1)
        case Many(before, mid, after, size)               =>
          if (n == length - after.size) after match {
            case Single(last)      => One(last)
            case Pair(first, last) => Many(Single(first), Empty, Single(last), 2)
          }
          else if (n == before.size) Many(mid.head, mid.drop(1), after, size - n)
          else {
            val n1     = n - before.size
            val midRem = mid.drop(n1 / 2)
            val tail   = midRem.drop(1)
            val head   = midRem.head
            if (n1 % 2 == 0) Many(head, tail, after, size - n)
            else Many(Single(head.last), tail, after, size - n)
          }
      }

  override def dropRight(n: Int): CherryTree[A] = {
    if (n <= 0) this
    else if (n >= length) Empty
    else if (n == length - 1) One(head)
    else
      this match {
        case Empty | One(_)                                => Empty
        case Many(before, mid, Pair(a, _), size) if n == 1 => Many(before, mid, Single(a), size - 1)
        case Many(before, mid, after, size)                =>
          if (n == length - before.size) before match {
            case Single(first)     => One(first)
            case Pair(first, last) => Many(Single(first), Empty, Single(last), 2)
          }
          else if (n == after.size) Many(before, mid.dropRight(1), mid.last, size - after.size)
          else {
            val n1     = n - after.size
            val midRem = mid.dropRight(n1 / 2)
            val init   = midRem.dropRight(1)
            val last   = midRem.last
            if (n1 % 2 == 0) Many(before, init, last, size - n)
            else Many(before, init, Single(last.first), size - n)
          }
      }
  }

  override def take(n: Int): CherryTree[A] = dropRight(length - n)

  override def takeRight(n: Int): CherryTree[A] = drop(length - n)

  def apply(i: Int): A = getOption(i).get

  def length: Int
  override def iterableFactory: SeqFactory[CherryTree] = CherryTree

  override def iterator: Iterator[A] = this match {
    case Empty                       => Iterator.empty
    case One(a)                      => Iterator.single(a)
    case Many(before, mid, after, _) =>
      before.iterator ++ mid.iterator.flatMap { case Pair(a, b) => Iterator(a, b) } ++ after.iterator
  }

  override def slice(from: Int, until: Int): CherryTree[A] = take(until).drop(from)

  override def appendedAll[B >: A](suffix: IterableOnce[B]): CherryTree[B] = suffix match {
    case c: CherryTree[B] =>
      c match {
        case One(value)                           => this :+ value
        case Many(beforeR, midR, afterR, lengthR) =>
          this match {
            case Empty                                => c
            case One(value)                           => value +: c
            case Many(beforeL, midL, afterL, lengthL) =>
              def merged[A](mid: CherryTree[Pair[B]]): CherryTree[B] =
                Many(beforeL, mid ++ midR, afterR, lengthL + lengthR)

              (afterL, beforeR) match {
                case (Single(a), Single(b))     => merged(midL :+ Pair(a, b))
                case (p1: Pair[B], p2: Pair[B]) => merged(midL :+ p1 :+ p2)
                case _ if lengthL < lengthR     => foldRight(c)(_ +: _)
                case _                          => c.foldLeft[CherryTree[B]](this)(_ :+ _)
              }
          }
        case Empty                                => this
      }
    case _                => super.appendedAll(suffix)
  }

  override protected[this] def className: String = "CherryTree"
}

object CherryTree extends StrictOptimizedSeqFactory[CherryTree] {
  case object Empty                  extends CherryTree[Nothing] {
    def length = 0
  }
  final case class One[+A](value: A) extends CherryTree[A] {
    def length = 1
  }
  final case class Many[+A](
    before: Peduncle[A],
    mid: CherryTree[Pair[A]],
    after: Peduncle[A],
    override val length: Int,
  ) extends CherryTree[A]

  def from[A](source: IterableOnce[A]): CherryTree[A] =
    source.iterator.foldLeft[CherryTree[A]](Empty)(_.appended(_))

  def empty[A]: CherryTree[A] = Empty

  def newBuilder[A]: Builder[A, CherryTree[A]] = new ReusableBuilder[A, CherryTree[A]] {
    private var tree: CherryTree[A] = Empty
    def addOne(elem: A): this.type  = {
      tree = tree.appended(elem)
      this
    }
    def clear(): Unit               = tree = Empty
    def result(): CherryTree[A]     = tree
  }
}

sealed trait Peduncle[+A] {
  def first: A
  def last: A
  def size: Int
  def iterator: Iterator[A]        = this match {
    case Single(first)       => Iterator.single(first)
    case Pair(first, second) => Iterator(first, second)
  }
  def getOption(i: Int): Option[A] = this match {
    case Single(first)       => if (i == 0) Some(first) else None
    case Pair(first, second) => if (i == 0) Some(first) else if (i == 1) Some(second) else None
  }
  def updatedWith[A1 >: A](f: A => A1)(i: Int): Peduncle[A1]
}
case class Single[+A](first: A)        extends Peduncle[A] {
  def size: Int = 1
  def last      = first

  override def updatedWith[A1 >: A](f: A => A1)(i: Int): Single[A1] = if (i == 0) Single(f(first)) else this
}
case class Pair[+A](first: A, last: A) extends Peduncle[A] {
  def size: Int                                                   = 2
  override def updatedWith[A1 >: A](f: A => A1)(i: Int): Pair[A1] =
    if (i == 0) Pair(f(first), last) else if (i == 1) Pair(first, f(last)) else this
}

package com.evolutiongaming.bootcamp.collections.trie

import scala.collection.immutable.IntMap
import scala.collection.immutable.AbstractMap
import scala.collection.IterableOps
import scala.collection.immutable.StrictOptimizedMapOps
import scala.collection.immutable.ArraySeq
import scala.collection.immutable.IndexedSeqOps
import scala.collection.IterableFactoryDefaults
import scala.collection.StrictOptimizedSeqFactory
import scala.collection.mutable.Builder
import scala.collection.SeqFactory
import scala.collection.mutable.ReusableBuilder
import scala.annotation.tailrec

sealed trait IntTrie[+A] extends Map[Int, A] {
  import IntTrie._
  protected def insert[A1 >: A](key: Int, mask: Int, value: A1): IntTrie[A1] = this match {
    case Empty             => Cell(key, value)
    case Cell(`key`, _)    => Cell(key, value)
    case c: Cell[A]        => c.extend(mask).insert(key, mask, value)
    case Branch(zero, one) =>
      if ((key & mask) == 0) Branch(zero.insert(key, mask >>> 1, value), one)
      else Branch(zero, one.insert(key, mask >>> 1, value))
  }

  protected def remove[A1 >: A](key: Int, mask: Int): IntTrie[A1] = this match {
    case Empty | Cell(`key`, _) => Empty
    case _: Cell[_]             => this
    case Branch(zero, one)      =>
      if ((key & mask) == 0) Branch(zero.remove(key, mask >>> 1), one)
      else Branch(zero, one.remove(key, mask >>> 1))
  }

  protected def getOption(key: Int, mask: Int): Option[A] = this match {
    case Cell(`key`, value)   => Some(value)
    case Branch(zero, one)    =>
      if ((key & mask) == 0) zero.getOption(key, mask >>> 1)
      else one.getOption(key, mask >>> 1)
    case Empty | (_: Cell[_]) => None
  }

  def iterator: Iterator[(Int, A)] = this match {
    case Empty             => Iterator.empty
    case Cell(key, value)  => Iterator.single(key -> value)
    case Branch(zero, one) => zero.iterator ++ one.iterator
  }

  def get(key: Int): Option[A] = getOption(key, 1 << 31)

  def removed(key: Int): IntTrie[A] = remove(key, 1 << 31)

  def updated[V1 >: A](key: Int, value: V1): Map[Int, V1] = insert(key, 1 << 31, value)

  override protected[this] def className = "IntTrie"

}

object IntTrie {
  case object Empty                                              extends IntTrie[Nothing]
  final case class Cell[+A](key: Int, value: A)                  extends IntTrie[A] {
    def extend(mask: Int): Branch[A] =
      if ((key & mask) == 0) Branch(this, Empty) else Branch(Empty, this)
  }
  final case class Branch[+A](zero: IntTrie[A], one: IntTrie[A]) extends IntTrie[A]
}

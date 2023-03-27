package com.evolutiongaming.bootcamp.collections

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

sealed trait IntTrie[+A] extends Map[Int, A] {
  import IntTrie._
  protected def insert[A1 >: A](key: Int, mask: Int, value: A1): IntTrie[A1] = this match {
    case Empty          => Cell(key, value)
    case Cell(`key`, _) => Cell(key, value)
    case c: Cell[A]     => c.extend(mask).insert(key, mask, value)
    case Branch(zero, one) =>
      if ((key & mask) == 0) Branch(zero.insert(key, mask >>> 1, value), one)
      else Branch(zero, one.insert(key, mask >>> 1, value))
  }

  protected def remove[A1 >: A](key: Int, mask: Int): IntTrie[A1] = this match {
    case Empty | Cell(`key`, _) => Empty
    case _: Cell[_]             => this
    case Branch(zero, one) =>
      if ((key & mask) == 0) Branch(zero.remove(key, mask >>> 1), one)
      else Branch(zero, one.remove(key, mask >>> 1))
  }

  protected def getOption(key: Int, mask: Int): Option[A] = this match {
    case Cell(`key`, value) => Some(value)
    case Branch(zero, one) =>
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
  case object Empty extends IntTrie[Nothing]
  final case class Cell[+A](key: Int, value: A) extends IntTrie[A] {
    def extend(mask: Int): Branch[A] =
      if ((key & mask) == 0) Branch(this, Empty) else Branch(Empty, this)
  }
  final case class Branch[+A](zero: IntTrie[A], one: IntTrie[A]) extends IntTrie[A]
}

sealed trait PackedIntTrie[+A] extends Map[Int, A] {
  import PackedIntTrie._

  def iterator: Iterator[(Int, A)] = this match {
    case Cell(key, value)       => Iterator.single(key -> value)
    case Branch(bitMap, values) => values.iterator.flatMap(_.iterator)
    case Empty                  => Iterator.empty
  }

  def getOption(key: Int, shift: Int): Option[A] = this match {
    case Cell(`key`, value) => Some(value)
    case Branch(bitMap, values) =>
      val skey = (key >>> shift) & MASK
      if ((bitMap & (1 << skey)) != 0)
        values(branchPos(bitMap, skey)).getOption(key, shift >>> BITS)
      else None
    case (_: Cell[_] | Empty) => None
  }

  def insert[A1 >: A](key: Int, value: A1, shift: Int): PackedIntTrie[A1] = this match {
    case Empty          => Cell(key, value)
    case Cell(`key`, _) => Cell(key, value)
    case c: Cell[A]     => c.extend(shift).insert(key, value, shift >>> BITS)
    case Branch(bitMap, children) =>
      val skey = (key >>> shift) & MASK
      if ((bitMap & (1 << skey)) != 0) {
        val pos = branchPos(bitMap, skey)
        val subTree = children(pos).insert(key, value, shift >>> BITS)
        val newChildren = children.updated(pos, subTree)
        Branch(bitMap, newChildren)
      } else {
        val bitMap1 = bitMap | (1 << skey)
        val pos = branchPos(bitMap1, skey)
        val newChildren = (children.take(pos) :+ Cell(key, value)) ++ children.drop(pos)
        Branch(bitMap1, newChildren)
      }
  }

  def remove[A1 >: A](key: Int, shift: Int): PackedIntTrie[A1] = this match {
    case Empty | Cell(`key`, _) => Empty
    case c: Cell[A]             => this
    case Branch(bitMap, children) =>
      val skey = (key >>> shift) & MASK
      if ((bitMap & (1 << skey)) == 0) this
      else {
        val pos = branchPos(bitMap, skey)
        children(pos).remove(key, shift >>> BITS) match {
          case Empty =>
            val bitMap1 = bitMap & ~(1 << skey)
            val newChildren = children.take(pos) ++ children.drop(pos + 1)
            if (bitMap1 == 0) Empty else Branch(bitMap1, newChildren)
          case subTree =>
            val newChildren = children.updated(pos, subTree)
            Branch(bitMap, newChildren)
        }
      }
  }

  def get(key: Int): Option[A] = getOption(key, 32 - BITS)

  def removed(key: Int): PackedIntTrie[A] = remove(key, 32 - BITS)

  def updated[V1 >: A](key: Int, value: V1): PackedIntTrie[V1] = insert(key, value, 32 - BITS)

  override protected[this] def className = "PackedIntTrie"
}

object PackedIntTrie {
  val BITS = 5
  val MASK: Int = (1 << BITS) - 1

  case object Empty extends PackedIntTrie[Nothing]
  case class Cell[+A](key: Int, value: A) extends PackedIntTrie[A] {
    def extend(shift: Int): Branch[A] = {
      val skey = (key >>> shift) & MASK
      val bitMap = 1 << skey
      val children = ArraySeq.tabulate(1 << BITS)(i => if (skey == i) this else Empty)
      Branch(bitMap, children)
    }
  }

  case class Branch[+A](bitMap: Int, children: ArraySeq[PackedIntTrie[A]]) extends PackedIntTrie[A]

  def branchPos(bitMap: Int, maskedKey: Int): Int = {
    val mask = ~((1 << maskedKey) - 1)
    Integer.bitCount(bitMap & mask)
  }
}

final class TrieVector[+A](trie: PackedIntTrie[A], offset: Int, val length: Int)
    extends IndexedSeq[A]
    with IndexedSeqOps[A, TrieVector, TrieVector[A]]
    with IterableFactoryDefaults[A, TrieVector] {
  def apply(i: Int): A = trie.get(i + offset).get

  override def appended[B >: A](elem: B): TrieVector[B] =
    new TrieVector(trie.updated(length + offset, elem), offset, length + 1)

  override def prepended[B >: A](elem: B): TrieVector[B] =
    new TrieVector(trie.updated(offset - 1, elem), offset - 1, length + 1)

  override def iterableFactory: SeqFactory[TrieVector] = TrieVector
}

object TrieVector extends StrictOptimizedSeqFactory[TrieVector] {
  def from[A](source: IterableOnce[A]): TrieVector[A] =
    source.iterator.foldLeft(empty[A])(_ :+ _)

  val emptyVector: TrieVector[Nothing] = new TrieVector(PackedIntTrie.Empty, 0, 0)
  def empty[A]: TrieVector[A] = emptyVector
  def newBuilder[A]: Builder[A, TrieVector[A]] = new ReusableBuilder[A, TrieVector[A]] {
    var trie = empty[A]
    def addOne(elem: A): this.type = {
      trie :+= elem
      this
    }
    def clear(): Unit = trie = empty[A]
    def result(): TrieVector[A] = trie
  }
}

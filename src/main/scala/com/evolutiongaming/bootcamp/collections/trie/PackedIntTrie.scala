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

sealed trait PackedIntTrie[+A] extends Map[Int, A] {
  import PackedIntTrie._

  def iterator: Iterator[(Int, A)] = this match {
    case Cell(key, value)       => Iterator.single(key -> value)
    case Branch(bitMap, values) => values.iterator.flatMap(_.iterator)
    case Empty                  => Iterator.empty
  }

  @tailrec final def getOption(key: Int, shift: Int): Option[A] = {
    val shift1 = shift.max(0)
    this match {
      case Cell(`key`, value) => Some(value)
      case Branch(bitMap, values) =>
        val skey = (key >>> shift1) & MASK
        if ((bitMap & (1 << skey)) != 0)
          values(branchPos(bitMap, skey)).getOption(key, shift1 - BITS)
        else None
      case (_: Cell[_] | Empty) => None
    }
  }

  def insert[A1 >: A](key: Int, value: A1, shift: Int): PackedIntTrie[A1] = {
    val shift1 = shift.max(0)
    this match {
      case Empty          => Cell(key, value)
      case Cell(`key`, _) => Cell(key, value)
      case c: Cell[A]     => c.extend(shift1).insert(key, value, shift1)
      case Branch(bitMap, children) =>
        val skey = (key >>> shift1) & MASK
        if ((bitMap & (1 << skey)) != 0) {
          val pos = branchPos(bitMap, skey)
          val subTree = children(pos).insert(key, value, shift1 - BITS)
          val newChildren = children.updated(pos, subTree)
          Branch(bitMap, newChildren)
        } else {
          val bitMap1 = bitMap | (1 << skey)
          val pos = branchPos(bitMap1, skey)
          val newChildren = (children.take(pos) :+ Cell(key, value)) ++ children.drop(pos)
          Branch(bitMap1, newChildren)
        }
    }
  }

  def remove[A1 >: A](key: Int, shift: Int): PackedIntTrie[A1] = {
    val shift1 = shift.max(0)
    this match {
      case Empty | Cell(`key`, _) => Empty
      case c: Cell[A]             => this
      case Branch(bitMap, children) =>
        val skey = (key >>> shift1) & MASK
        if ((bitMap & (1 << skey)) == 0) this
        else {
          val pos = branchPos(bitMap, skey)
          children(pos).remove(key, shift1 - BITS) match {
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
      val children = ArraySeq(this)
      Branch(bitMap, children)
    }
  }

  case class Branch[+A](bitMap: Int, children: ArraySeq[PackedIntTrie[A]]) extends PackedIntTrie[A]

  def branchPos(bitMap: Int, maskedKey: Int): Int = {
    val mask = (1 << maskedKey) - 1
    Integer.bitCount(bitMap & mask)
  }
}

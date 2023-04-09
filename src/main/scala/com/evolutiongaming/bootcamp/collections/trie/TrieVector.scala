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

final class TrieVector[+A](trie: PackedIntTrie[A], offset: Int, val length: Int)
    extends IndexedSeq[A]
    with IndexedSeqOps[A, TrieVector, TrieVector[A]]
    with IterableFactoryDefaults[A, TrieVector] {
  def apply(i: Int): A = trie.get(i + offset).getOrElse(throw new IndexOutOfBoundsException(i))

  override def appended[B >: A](elem: B): TrieVector[B] =
    new TrieVector(trie.updated(length + offset, elem), offset, length + 1)

  override def prepended[B >: A](elem: B): TrieVector[B] =
    new TrieVector(trie.updated(offset - 1, elem), offset - 1, length + 1)

  override def iterableFactory: SeqFactory[TrieVector] = TrieVector

  override protected[this] def className = "TrieVector"
}

object TrieVector extends StrictOptimizedSeqFactory[TrieVector] {
  def from[A](source: IterableOnce[A]): TrieVector[A] =
    source.iterator.foldLeft(empty[A])(_ :+ _)

  val emptyVector: TrieVector[Nothing]         = new TrieVector(PackedIntTrie.Empty, 0, 0)
  def empty[A]: TrieVector[A]                  = emptyVector
  def newBuilder[A]: Builder[A, TrieVector[A]] = new ReusableBuilder[A, TrieVector[A]] {
    var trie                       = empty[A]
    def addOne(elem: A): this.type = {
      trie :+= elem
      this
    }
    def clear(): Unit              = trie = empty[A]
    def result(): TrieVector[A]    = trie
  }
}

object Test extends App {
  var init = TrieVector.empty[Int]

  init :+= 1
  init +:= 2
  println(init)
}

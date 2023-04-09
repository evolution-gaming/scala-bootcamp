package com.evolutiongaming.bootcamp.collections.trie

import scala.collection.immutable.MapOps
import scala.collection.StrictOptimizedIterableOps
import scala.collection.MapFactory
import scala.collection.mutable.Builder
import scala.collection.immutable.AbstractMap
import scala.collection.MapFactoryDefaults
import scala.collection.immutable
import scala.collection.mutable.ReusableBuilder

final class TrieHashMap[K, +V](values: PackedIntTrie[List[(K, V)]])
    extends AbstractMap[K, V]
    with MapOps[K, V, TrieHashMap, TrieHashMap[K, V]]
    with MapFactoryDefaults[K, V, TrieHashMap, immutable.Iterable] {

  override def iterator: Iterator[(K, V)] =
    values.valuesIterator.flatMap(_.iterator)

  override def get(key: K): Option[V] =
    values
      .get(key.hashCode())
      .flatMap(_.collectFirst { case (`key`, value) => value })

  override def removed(key: K): TrieHashMap[K, V] = {
    val hash = key.hashCode()
    values.get(hash) match {
      case None       => this
      case Some(list) =>
        new TrieHashMap(values.updated(hash, list.filter(_._1 != key)))
    }
  }

  override def updated[V1 >: V](key: K, value: V1): TrieHashMap[K, V1] = {
    val hash = key.hashCode()
    val list = values.getOrElse(key.hashCode, Nil)
    new TrieHashMap(
      values.updated(hash, (key, value) :: list.filter(_._1 != key))
    )
  }

  override def mapFactory = TrieHashMap

  override protected[this] def className = "TrieHashMap"
}

object TrieHashMap extends MapFactory[TrieHashMap] {

  override def empty[K, V]: TrieHashMap[K, V] = new TrieHashMap(
    PackedIntTrie.Empty
  )

  override def from[K, V](it: IterableOnce[(K, V)]): TrieHashMap[K, V] =
    it.iterator.foldLeft(empty[K, V]) { case (map, (k, v)) =>
      map.updated(k, v)
    }

  override def newBuilder[K, V]: Builder[(K, V), TrieHashMap[K, V]] =
    new ReusableBuilder[(K, V), TrieHashMap[K, V]] {
      private var map                              = empty[K, V]
      override def addOne(elem: (K, V)): this.type = {
        map = map.updated(elem._1, elem._2)
        this
      }
      override def clear(): Unit                   = map = empty
      override def result(): TrieHashMap[K, V]     = map
    }

}

object Test2 extends App {

  def words() = {
    val rnd = new scala.util.Random(87654321)

    Iterator.fill(5000)(Array.fill(2)(rnd.nextPrintableChar()).mkString)
  }

  def collectWords[M[k, +v] <: MapOps[k, v, M, M[k, v]]](factory: {
    def empty[K, V]: M[K, V]
  }): M[String, Int] =
    words()
      .foldLeft(factory.empty[String, Int]) { case (map, word) =>
        map.updated(word, map.getOrElse(word, 0) + 1)
      }

  val x = collectWords(TrieHashMap)
  println(x)
}

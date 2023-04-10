package com.evolutiongaming.bootcamp.collections

import scala.collection.LinearSeqOps
import scala.collection.LinearSeq
import scala.collection.SeqFactory
import scala.collection.mutable.Builder
import scala.collection.IterableFactoryDefaults
import scala.collection.mutable.ReusableBuilder
sealed trait MyList[+A]
    extends LinearSeq[A]
    with LinearSeqOps[A, MyList, MyList[A]]
    with IterableFactoryDefaults[A, MyList] {
  override def iterableFactory = MyList

  private def reverseWith[A1 >: A](acc: MyList[A1]): MyList[A1] = this match {
    case MyList.Nil        => acc
    case MyList.Cons(h, t) => t.reverseWith(MyList.Cons(h, acc))
  }

  def ::[A1 >: A](x: A1) = MyList.Cons(x, this)

  override def reverse: MyList[A] = reverseWith(MyList.Nil)

  override def isEmpty: Boolean = this.eq(MyList.Nil)

  override protected[this] def className: String = "MyList"
}

object MyList extends SeqFactory[MyList] {

  case object Nil extends MyList[Nothing]

  final case class Cons[+A](
    override val head: A,
    override val tail: MyList[A],
  ) extends MyList[A]

  def from[A](source: IterableOnce[A]): MyList[A] =
    source.iterator.foldRight[MyList[A]](Nil)(Cons(_, _))

  def empty[A] = Nil

  def newBuilder[A]: Builder[A, MyList[A]] = new ReusableBuilder[A, MyList[A]] {
    private var elems: MyList[A]   = Nil
    def addOne(elem: A): this.type = {
      elems = Cons(elem, elems)
      this
    }
    def clear(): Unit              = elems = Nil
    def result(): MyList[A]        = elems.reverse
  }
}

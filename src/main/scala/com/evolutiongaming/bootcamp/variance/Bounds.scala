package com.evolutiongaming.bootcamp.variance

import com.evolutiongaming.bootcamp.variance.Variance.InvariantBox

// https://docs.scala-lang.org/tour/upper-type-bounds.html
object Bounds {

  sealed trait Fruit

  sealed trait Apple     extends Fruit
  case object RedApple   extends Apple
  case object GreenApple extends Apple

  case object Orange extends Fruit



  // Type bounds are needed if you want to restrict type parameters in order to get more information about them



  // Upper type bound restricts type parameter to be a subtype of some other type

  def box1(head: Fruit, tail: Fruit*): List[Fruit] = head :: tail.toList
  val fruits: List[Fruit] = box1(RedApple, GreenApple)
//  val apples: List[Apple] = box1(RedApple, GreenApple)

  def box2[A <: Fruit](head: A, tail: A*): List[A] = head :: tail.toList
  var fruit2: List[Fruit] = box2(RedApple, GreenApple, Orange)
  val apples: List[Apple] = box2(RedApple, GreenApple)
  box2(Orange)
  fruit2 = apples



  // Lower type bound restricts type parameter to be a supertype of the given type
  def addToBox1[A](box: List[A], value: A): List[A] = value :: box

//  addToBox1[Apple](apples, Orange)

  def addToBox2[A, B >: A](box: List[A], value: B): List[B] = value :: box

  addToBox2[Apple, Fruit](apples, Orange)



  // Sometimes you may want to check type boundaries dynamically
  // Scala has two handy classes for that: <:< (upper bound) and =:= (strict match)


  final implicit class OptionOps[A](private val value: Option[A]) extends AnyVal {

    def plus(x: Int)(implicit ev: A =:= Int): Option[Int] = value.map(ev).map(_ + x)

    def bound[B](implicit ev: A <:< Option[B]): Option[B] = value.flatMap(ev)
  }

  final implicit class OptionIntOps(private val value: Option[Int]) extends AnyVal {
    def plus2(x: Int): Option[Int] = value.map(_ + x)
  }

  def test: InvariantBox[Int] = InvariantBox(List(throw new Exception("test")))

  Option(2).plus(3) // Some(5)
  Option(2).plus2(3)
  Option.empty[Nothing].plus(3)
//  Option("string").plus(3)
//  Option("string").plus(3)

  Option(Some(123)).bound // Option[Int]
//  Option(123).bound
}

package com.evolutiongaming.bootcamp.cats.v4

object p2_Monoid {

  /** Monoid[A] is semigroup with identity element, called "empty", also often referred as "zero"
    * (empty combine a) <-> a
    * (a combine empty) <-> a
    */
  import cats.Monoid

  /** There can be multiple possible monoids for the same type.
    *
    * Ex 2.0 implement a monoid with `*` (multiplication) as an operation.
    * Q: Can division be picked as an associative binary operation?
    */
  val intMultiplicationMonoid: Monoid[Int] = new Monoid[Int] {
    override def empty: Int = ???

    override def combine(x: Int, y: Int): Int = ???
  }

  /** Ex 2.1 use string concatenation as an operation
    */
  val stringMonoid: Monoid[String] = new Monoid[String] {
    override def empty: String = ???

    override def combine(x: String, y: String): String = ???
  }

  /** Ex 2.2 How about a monoid for boolean?
    * Pick AND as a binary operation.
    *
    * Q: How many monoids exists for boolean?
    */

  def boolMonoid: Monoid[Boolean] = Monoid.instance[Boolean](
    ???,
    (x: Boolean, y: Boolean) => ???,
  )

  // as you might already guessed, there are plenty of instances already defined in cats library:
  implicitly[Monoid[Option[Int]]]
  implicitly[Monoid[String]]
  implicitly[Monoid[Seq[Any]]]
  implicitly[Monoid[Map[Any, String]]]

  // Lets try to create Monoid for Nel
  import cats.data.NonEmptyList
  def monoidNel: Monoid[NonEmptyList[Int]] = ???

  // syntax same as for semigroup
  import cats.syntax.monoid._
  1 combine 2
  1 |+| 2
}

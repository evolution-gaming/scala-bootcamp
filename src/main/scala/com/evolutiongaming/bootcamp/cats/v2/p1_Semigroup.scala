package com.evolutiongaming.bootcamp.cats.v2

object p1_Semigroup {

  /**
    * Semigroup is Set with an associative binary operation.
    * Quick re-cap on associativity: 1 + (2 + 3) == (1 + 2) + 3
    * Meet the Semigroup type class:
    * */
  import cats.Semigroup

  /**
    * Ex 1.0 implement a semigroup with string concatenation as an associative binary operation
    * */
  val stringSemigroup: Semigroup[String] = new Semigroup[String] {
    override def combine(x: String, y: String): String =
      ??? /* your code here */
  }

  /**
    * Ex 1.1 implement a semigroup with `sum` as an operation.
    * Q: Can you pick another operation that forms a semigroup for ints?
    * */
  val intSemigroup: Semigroup[Int] = (a, b) => ??? /* your code here */

  /**
    * Ex 1.2
    * */
  def listSemigroup[A]: Semigroup[List[A]] =
    (a, b) => ??? /* your code here */

  // Cats has instances for a variety of types. They may be found in cats.instances package:
  import cats.instances.int._
  import cats.instances.option._
  import cats.syntax.semigroup._

  val catsIntSemigroup: Semigroup[Int] = implicitly[Semigroup[Int]]

  val result = 1 combine 2 //  = 3
  // we also can use a shorter alias
  val result2 = 1 |+| 2

  val optResult: Option[Int] = Option(3) |+| Option(7) // Some(10)


  /**
    * That's nice, but why can't we just use ordinary `+` defined for numerical types, for example?
    * For sure we can.
    * Semigroup may be useful if our goal is to provide a convenient rule for aggregating values of some custom type.
    * Or while designing a library | class we may demand a presence of Semigroup to be able to combine values.
    * For example, if we want to combine messages of Response type from different services into a single fat Response
    * without event knowing what's inside.
    *
    * */
}

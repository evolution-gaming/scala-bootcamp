package com.evolutiongaming.bootcamp.cats.v4

object p1_Semigroup {

  /** Semigroup for type A is an associative operation, called `combine`.
    * Associative: (a combine (b combine c)) == ((a combine b) combine c)
    *              1 + (2 + 3) == (1 + 2) + 3
    * Meet the Semigroup type class:
    */
  import cats.Semigroup

  // class EvoSemigroup[A] extends Semigroup[A] {}

  /** Ex 1.0 implement a semigroup with string concatenation as an associative binary operation
    */
  val stringSemigroup: Semigroup[String] = new Semigroup[String] {
    override def combine(x: String, y: String): String =
      ??? /* your code here */
  }

  /** Ex 1.1 implement a semigroup with `sum` as an operation.
    * Q: Can you pick another operation that forms a semigroup for ints?
    */
  val intSemigroup: Semigroup[Int] = (a, b) => ??? /* your code here */

  /** Ex 1.2 implement a semigroup  for list of A via concatenation
    */
  def listSemigroup[A]: Semigroup[List[A]] =
    Semigroup.instance((a, b) => ??? /* your code here */ )

  // Cats has instances for a variety of types.
  import cats.instances.int._
  import cats.syntax.semigroup._
  // import cats.implicits._

  val catsIntSemigroup: Semigroup[Int] = implicitly[Semigroup[Int]]

  // non existing
  // final case class User()
  // implicitly[Semigroup[User]]

  val result: Int  = 1 combine 2 //  = 3
  // we also can use a shorter alias
  val result2: Int = 1 |+| 2

  import cats.syntax.option._

  val optResult: Option[Int]     = Option(3) |+| Option(7) // Some(10)
  val someOptResult: Option[Int] = 3.some |+| 7.some // Some(10)

  import cats.data.NonEmptyList

  /** Ex 1.3 implement a semigroup  for Nel of Int via concatenation
    */
  def semigroupNel: Semigroup[NonEmptyList[Int]] = ???

  /** Ex 1.3 implement a semigroup  for Map via concatenation
    */
  def semigroupMap: Semigroup[Map[Int, Int]] = ???

  /** That's nice, but why can't we just use ordinary `+` defined for numerical types, for example?
    * For sure we can.
    * Semigroup may be useful if our goal is to provide a convenient rule for aggregating values of some custom type.
    * Or while designing a library | class we may demand a presence of Semigroup to be able to combine values.
    * For example, if we want to combine messages of Response type from different services into a single fat Response
    * without event knowing what's inside.
    */
}

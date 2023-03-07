package com.evolutiongaming.bootcamp.cats.v4

import cats.syntax.either._
import cats.syntax.option._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class p7_TraverseSpec extends AnyFlatSpec with Matchers {

  /** Ex 7.0 implement traverse function for Option. At least 1 None means result None.
    */
  def optionTraverse[A](input: List[Option[A]]): Option[List[A]] = ??? /* your code here */

  "Traverse for List of Option of A" should "be implemented correctly" in {
    optionTraverse(List.empty) shouldBe List.empty.some
    optionTraverse(List(1.some, none, 2.some)) shouldBe None
    optionTraverse(List(1.some, 4.some, 2.some)) shouldBe List(1, 4, 2).some
  }

  /** Ex 7.1 implement traverse for Either. Use fail fast approach (the first error encountered is returned).
    */
  def eitherTraverse[E, A](input: List[Either[E, A]]): Either[E, List[A]] = ??? /* your code here */

  "Traverse for List of Either of A" should "be implemented correctly" in {
    eitherTraverse(List.empty) shouldBe List.empty.asRight
    eitherTraverse(List(1.asRight, "Error".asLeft, 2.asRight, "Later error".asLeft)) shouldBe "Error".asLeft
    eitherTraverse(List(1.asRight, 4.asRight, 2.asRight)) shouldBe List(1, 4, 2).asRight
  }
}

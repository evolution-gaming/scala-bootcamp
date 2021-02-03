package com.evolutiongaming.bootcamp.basics

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import LinkedList._

import scala.util.Random

class CollectionsSpec extends AnyFlatSpec with ScalaCheckDrivenPropertyChecks {

  "findGap" should "find gap" in {
    findGap(List(1, 2, 3, 5, 6)) shouldEqual Some(3, 5)
  }

  "findGap" should "work correctly on empty" in {
    findGap(List.empty) shouldEqual None
  }

  "findGap" should "work correctly on no gaps" in {
    findGap((1 to 100).toList) shouldEqual None
  }

  "min" should "work correctly on empty" in {
    min(Nil) shouldEqual None
  }

  "min" should "work correctly on non empty" in {
    min(Random.shuffle(1 to 100).toList) shouldEqual Some(1)
  }

  "scanLeft" should "work correctly on numbers" in {
    val numbers = (1 to 100).toList
    scanLeft(0)(numbers)(_ + _) shouldEqual numbers.scanLeft(0)(_ + _)
  }

  "scanLeft" should "work correctly on letters" in {
    val letters = ('a' to 'z').toList.map(_.toString)
    scanLeft("")(letters)(_ + _) shouldEqual letters.scanLeft("")(_ + _)
  }

  "count" should "pass" in {
    count("aaaabbbcca") shouldEqual List(('a', 4), ('b', 3), ('c', 2), ('a', 1))
  }
}

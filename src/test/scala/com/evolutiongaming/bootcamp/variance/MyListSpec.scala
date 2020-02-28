package com.evolutiongaming.bootcamp.variance

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

final class MyListSpec extends AnyFlatSpec with Matchers {
  import MyList._

  "MyList" should "can be created empty" in {
    MyList.empty[String] mustBe Nil
  }

  it should "can be created from Seq" in {
    MyList(1, 2, 3) mustBe Cons(1, Cons(2, Cons(3, Nil)))
  }

  it should "can be mapped over" in {
    MyList(1, 2, 3).map(_.toString) mustBe Cons("1", Cons("2", Cons("3", Nil)))
  }

  it should "can prepend a value" in {
    MyList(2, 3).prepend(1) mustBe Cons(1, Cons(2, Cons(3, Nil)))
  }

  it should "can sum Int values" in {
    MyList(1, 2, 3).sum mustBe 6
  }

  it should "sum only Ints" in {
    """MyList("1", "2", "3").sum""" mustNot compile
  }
}

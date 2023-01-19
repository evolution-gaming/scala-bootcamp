package com.evolutiongaming.bootcamp.basics

import com.evolutiongaming.bootcamp.basics.ClassesAndTraits._
import org.scalacheck.Gen
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class ClassesAndTraitsSpec extends AnyFlatSpec with ScalaCheckDrivenPropertyChecks {
  "totalBalance" should "correctly calculate total balance for empty list" in {
    totalBalance(List.empty).balance shouldBe 0
  }

  it should "correctly calculate total balance for list of Users" in {
    val numberGen = Gen.choose[Int](1, 10)
    val loginGen = Gen.oneOf("potter", "smith", "jonson")

    forAll(numberGen, loginGen) { (number, login) =>
      val users = List.fill(number)(RegularUser(login, 1.0))

      totalBalance(users).balance shouldBe number.toDouble
    }
  }

  it should "correctly calculate total balance for unknown entities" in {
    val entity1 = new HasBalance { def balance: Double = 100 }
    val entity2 = new HasBalance { def balance: Double = 200 }
    val entity3 = new HasBalance { def balance: Double = 500 }

    totalBalance(List(entity1, entity2, entity3)) shouldBe 800
  }
}

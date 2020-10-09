package com.evolutiongaming.bootcamp.typeclass

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class ImplicitsSpec extends AnyFreeSpec with Matchers {
  "Exercise 1" - {
    "isBce extension method exists" in {
      assertCompiles(
        """import com.evolutiongaming.bootcamp.typeclass.Implicits.Exercise1._
          |import com.evolutiongaming.bootcamp.typeclass.Implicits.Exercise1.Implicits._
          |
          |CommonEraStart.isBce
          |""".stripMargin,
      )
    }
    "isCe function works as expected" in {
      import com.evolutiongaming.bootcamp.typeclass.Implicits.Exercise1._

      assert(Workspace.isCe(CommonEraStart))
      assert(Workspace.isCe(CommonEraStart.plusSeconds(1L)))
      assert(!Workspace.isCe(CommonEraStart.minusSeconds(1L)))
    }
  }

  "Exercise 2" - {
    import com.evolutiongaming.bootcamp.typeclass.Implicits.Exercise2._
    import com.evolutiongaming.bootcamp.typeclass.Implicits.MoreImplicitParameters._

    case class TestType(showString: String)
    implicit val testTypeShow: Show[TestType] = (value: TestType) => value.showString

    "reverseShow should return reversed result of show" in {
      reverseShow(TestType("abc")) shouldEqual "cba"
    }
  }

  "Exercise 3" - {
    import com.evolutiongaming.bootcamp.typeclass.Implicits.Exercise3._

    "secondBiggestValue" in {
      secondBiggestValue(List.empty[Int]) shouldEqual None
      secondBiggestValue(List(1)) shouldEqual None
      secondBiggestValue(Vector(1, 3, 2)) shouldEqual Some(2)
      secondBiggestValue(Vector(
        HDEYears(1),
        HDEYears(3),
        HDEYears(2),
      )) shouldEqual Some(HDEYears(2))
    }

    "average" in {
      average(List.empty[Double]) shouldEqual None
      average(List(BigDecimal("1.0"), BigDecimal("2.0"))) shouldEqual Some(BigDecimal("1.5"))
      val customAvg = average(Vector(CustomNumber(1F), CustomNumber(2F)))
      assert(customAvg.exists(_.value > 1F))
      assert(customAvg.exists(_.value < 2F))
    }
  }
}

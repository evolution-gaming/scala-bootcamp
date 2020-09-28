package com.evolutiongaming.bootcamp.error_handling

import cats.syntax.all._
import com.evolutiongaming.bootcamp.error_handling.ErrorHandling._
import org.scalacheck.Gen._
import org.scalatest.Assertion
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class ErrorHandlingSpec
  extends AnyFlatSpec
    with Matchers
    with ScalaCheckDrivenPropertyChecks {

  "parseIntOption" should "handle valid and invalid strings" in {
    forAll { x: Int =>
      parseIntOption(x.toString) shouldBe Some(x)
    }
    forAll(alphaStr) { x: String =>
      parseIntOption(x) shouldBe None
    }
  }

  "parseIntEither" should "handle valid and invalid strings" in {
    forAll { x: Int =>
      parseIntEither(x.toString) shouldBe Right(x)
    }
    forAll(alphaStr) { x: String =>
      parseIntEither(x) shouldBe Left(s"$x does not contain an integer")
    }
  }

  "credit" should "handle valid and invalid amounts" in {
    import TransferError._
    forAll(choose(Int.MinValue, -1)) { x: Int =>
      credit(x) shouldBe Left(NegativeAmount)
    }
    credit(0) shouldBe Left(ZeroAmount)
    forAll(choose(1000000, Int.MaxValue)) { x: Int =>
      credit(x) shouldBe Left(AmountIsTooLarge)
    }
    forAll(choose(1, 999999), choose(1, 999)) { (a: Int, b: Int) =>
      credit(BigDecimal("%d.%03d".format(a, b))) shouldBe Left(TooManyDecimals)
    }
    forAll(choose(1, 999999), choose(1, 99)) { (a: Int, b: Int) =>
      credit(BigDecimal("%d.%02d".format(a, b))) shouldBe Right(())
    }
  }

  "StudentValidator" should "handle valid and invalid students" in {
    import ValidationError._

    StudentValidator.validate(
      username = "username",
      age = "33",
    ) shouldBe Student("username", 33).validNec

    def checkInvalid(username: String, age: String, errors: Set[ValidationError]): Assertion =
      StudentValidator.validate(
        username = username,
        age = age,
      ).leftMap(_.toList.toSet) shouldBe errors.invalid

    checkInvalid(
      username = "a",
      age = "33",
      errors = Set(UsernameLengthIsInvalid),
    )
    checkInvalid(
      username = "@",
      age = "33",
      errors = Set(UsernameLengthIsInvalid, UsernameHasSpecialCharacters),
    )
    checkInvalid(
      username = "a",
      age = "invalid",
      errors = Set(UsernameLengthIsInvalid, AgeIsNotNumeric),
    )
    checkInvalid(
      username = "@",
      age = "2",
      errors = Set(UsernameLengthIsInvalid, UsernameHasSpecialCharacters, AgeIsOutOfBounds),
    )
    checkInvalid(
      username = "@@@",
      age = "2",
      errors = Set(UsernameHasSpecialCharacters, AgeIsOutOfBounds),
    )
  }
}

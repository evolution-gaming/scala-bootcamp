package com.evolutiongaming.bootcamp.basics

import ControlStructures._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import org.scalacheck.Gen._
import org.scalatest.Assertion
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

import scala.util.Try

class ControlStructuresSpec extends AnyFlatSpec with ScalaCheckDrivenPropertyChecks {
  private def checkFizzBuzz(f: Int => String): Assertion = {
    val obtained = (0 to 100).toList map f
    // we're doing it this way here to avoid providing the answer to the exercise by implementing the function
    val expected = List("fizzbuzz", "1", "2", "fizz", "4", "buzz", "fizz", "7", "8", "fizz", "buzz", "11", "fizz", "13", "14", "fizzbuzz", "16", "17", "fizz", "19", "buzz", "fizz", "22", "23", "fizz", "buzz", "26", "fizz", "28", "29", "fizzbuzz", "31", "32", "fizz", "34", "buzz", "fizz", "37", "38", "fizz", "buzz", "41", "fizz", "43", "44", "fizzbuzz", "46", "47", "fizz", "49", "buzz", "fizz", "52", "53", "fizz", "buzz", "56", "fizz", "58", "59", "fizzbuzz", "61", "62", "fizz", "64", "buzz", "fizz", "67", "68", "fizz", "buzz", "71", "fizz", "73", "74", "fizzbuzz", "76", "77", "fizz", "79", "buzz", "fizz", "82", "83", "fizz", "buzz", "86", "fizz", "88", "89", "fizzbuzz", "91", "92", "fizz", "94", "buzz", "fizz", "97", "98", "fizz", "buzz")
    obtained shouldEqual expected
  }

  "fizzBuzz1" should "work correctly" in {
    checkFizzBuzz(fizzBuzz1)
  }

  "fizzBuzz2" should "work correctly" in {
    checkFizzBuzz(fizzBuzz2)
  }

  "applyNTimesForInts" should "work correctly" in {
    forAll(choose(1, 1000), choose(1, 1000)) {
      case (a: Int, b: Int) =>
        applyNTimesForInts(_ + 1, a)(b) shouldEqual a + b
    }
  }

  "applyNTimes" should "work correctly" in {
    forAll(choose(1, 1000), choose(1, 10000), choose(1, 500)) { case (n, a, b) =>
      applyNTimes[Int](_ + b, n)(a) shouldEqual a + n * b
    }
  }

  "makeTransfer" should "work correctly" in {
    val testUserService = new UserService {
      // TODO: consider not having the balance encoded in the name

      def validateUserName(name: String): Either[ErrorMessage, Unit] = {
        // Test implementation provided, don't change
        if (name.forall(x => x.isLetterOrDigit || x == '.')) Right(()) else Left(s"User $name is invalid")
      }

      def findUserId(name: String): Either[ErrorMessage, UserId] = {
        // Test implementation provided, don't change
        if (name != "invalid") Right(s"userid.$name") else Left(s"User $name not found")
      }

      def validateAmount(amount: Amount): Either[ErrorMessage, Unit] = {
        // Test implementation provided, don't change
        if (amount > 0) Right(()) else Left(s"Amount $amount is not positive")
      }

      def findBalance(userId: UserId): Either[ErrorMessage, Amount] = {
        // Test implementation provided, don't change
        if (userId.startsWith("userid.")) {
          val lastSegment = userId.split("\\.").lastOption
          val value = lastSegment.flatMap(x => Try(x.toInt).toOption).getOrElse(0)
          Right(BigDecimal(value))
        } else {
          Left(s"Invalid user ID $userId")
        }
      }

      // Upon success, returns the remaining amount
      def updateAccount(userId: UserId, previousBalance: Amount, delta: Amount): Either[ErrorMessage, Amount] = {
        // Test implementation provided, don't change
        for {
          balance <- findBalance(userId)
          result <- if (balance == previousBalance)
            Right[ErrorMessage, Amount](previousBalance + delta)
          else
            Left(s"previousBalance was expected to be $balance  but was provided as $previousBalance")
        } yield result
      }
    }

    makeTransfer(testUserService, "valid.200", "valid.25", 50) shouldEqual Right((150, 75))
    makeTransfer(testUserService, "valid.10", "valid.20", 7) shouldEqual Right((3, 27))
    makeTransfer(testUserService, "invalid", "valid.200", 50) shouldBe a[Left[_, _]]
  }

  "AProductB" should "contain all the elements in `A * B`" in {
    AProductB should contain theSameElementsAs Set(
      (0, true),
      (1, true),
      (2, true),
      (0, false),
      (1, false),
      (2, false),
    )
  }

  "ASumB" should "contain all the elements in `A + B`" in {
    ASumB should contain theSameElementsAs Set(
      Left(0), Left(1), Left(2),
      Right(true), Right(false)
    )
  }

}

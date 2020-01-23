package com.evolutiongaming.bootcamp.basics

import ControlStructures._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import org.scalacheck.Gen._
import org.scalatest.Assertion
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

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

  private class TestUserService extends UserService { // not thread safe
    private var balances: Map[UserId, Amount] = Map.empty

    // test update that doesn't care about previous values
    def updateAccount(name: String, amount: Amount): Either[ErrorMessage, Unit] = {
      for {
        userId  <- findUserId(name)
        balance <- findBalance(userId)
        _       <- updateAccount(userId, balance, balance + amount)
      } yield ()
    }

    def validateUserName(name: String): Either[ErrorMessage, Unit] = {
      if (name.forall(x => x.isLetter)) Right(()) else Left(s"User $name is invalid")
    }

    def findUserId(name: String): Either[ErrorMessage, UserId] = {
      if (name != "invalid") Right(s"userid.$name") else Left(s"User $name not found")
    }

    def validateAmount(amount: Amount): Either[ErrorMessage, Unit] = {
      if (amount > 0) Right(()) else Left(s"Amount $amount is not positive")
    }

    def findBalance(userId: UserId): Either[ErrorMessage, Amount] = {
      if (userId.startsWith("userid.")) {
        Right(balances.getOrElse(userId, 0))
      } else {
        Left(s"Invalid user ID $userId")
      }
    }

    // Upon success, returns the resulting balance
    def updateAccount(userId: UserId, previousBalance: Amount, delta: Amount): Either[ErrorMessage, Amount] = {
      for {
        balance <- findBalance(userId)
        result <- if (balance == previousBalance) {
          val newBalance = previousBalance + delta
          balances = balances + (userId -> newBalance)
          Right[ErrorMessage, Amount](newBalance)
        } else {
          Left(s"previousBalance was expected to be $balance  but was provided as $previousBalance")
        }
      } yield result
    }
  }

  "makeTransfer" should "work correctly in 200 - 50 => 25 + 50" in {
    val service = new TestUserService
    val obtained = for {
      _       <- service.updateAccount("a", 200)
      _       <- service.updateAccount("b", 25)
      result  <- makeTransfer(service, "a", "b", 50)
    } yield result
    obtained shouldEqual Right((150, 75))
  }

  it should "work correctly in 10 - 7 => 20 + 7" in {
    val service = new TestUserService
    val obtained = for {
      _       <- service.updateAccount("a", 10)
      _       <- service.updateAccount("b", 20)
      result  <- makeTransfer(service, "a", "b", 7)
    } yield result
    obtained shouldEqual Right((3, 27))
  }

  it should "detect incorrect userIds" in {
    val service = new TestUserService
    val obtained = makeTransfer(service, "invalid", "valid", 50)
    obtained shouldBe a[Left[_, _]]
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
      Right(true), Right(false),
    )
  }

}

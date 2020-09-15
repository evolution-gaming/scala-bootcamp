package com.evolutiongaming.bootcamp.adt

import com.evolutiongaming.bootcamp.adt.AlgebraicDataTypes.PaymentMethod._
import com.evolutiongaming.bootcamp.adt.AlgebraicDataTypes._
import org.scalacheck.Gen._
import org.scalatest.{EitherValues, OptionValues}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class AlgebraicDataTypesSpec
  extends AnyFlatSpec
    with OptionValues
    with EitherValues
    with ScalaCheckDrivenPropertyChecks {

  "GameLevel" should "allow being created with levels between 1 and 80" in {
    forAll(choose(min = 1, max = 80)) { v: Int =>
      GameLevel.create(v).value.value shouldBe v
    }
  }

  it should "forbid being created with invalid levels" in {
    forAll { v: Int =>
      whenever(v < 1 || v > 80) {
        GameLevel.create(v) shouldBe None
      }
    }
  }

  "Time" should "allow being created with valid hour and minute values" in {
    forAll(choose(min = 0, max = 23), choose(min = 0, max = 59)) { (hour: Int, minute: Int) =>
      val time = Time.create(hour, minute).getOrElse(sys.error("Expected 'Right'"))
      time.hour shouldBe hour
      time.minute shouldBe minute
    }
  }

  it should "forbid being created with invalid hour values" in {
    forAll { hour: Int =>
      whenever(hour < 0 || hour > 23) {
        Time.create(hour = hour, minute = 30).left.value shouldBe "Invalid hour value"
      }
    }
  }

  it should "forbid being created with invalid minute values" in {
    forAll { minute: Int =>
      whenever(minute < 0 || minute > 59) {
        Time.create(hour = 12, minute = minute).left.value shouldBe "Invalid minute value"
      }
    }
  }

  "PaymentService" should "process payments correctly" in {
    val paymentService = new PaymentService(
      new TestBankAccountService,
      new TestCreditCardService,
      new TestCashService,
    )
    paymentService.processPayment(
      amount = 12.34,
      method = BankAccount(AccountNumber("123ABC")),
    ) shouldBe PaymentStatus("Sent 12.34 to account number 123ABC")
    paymentService.processPayment(
      amount = 43.21,
      method = CreditCard(CardNumber("123...53"), ValidityDate(12, 21)),
    ) shouldBe PaymentStatus("Sent 43.21 to card number 123...53")
    paymentService.processPayment(
      amount = 100,
      method = Cash,
    ) shouldBe PaymentStatus("Sent 100 in cash")
  }

  private class TestBankAccountService extends BankAccountService {
    override def processPayment(amount: BigDecimal, accountNumber: AccountNumber): PaymentStatus =
      PaymentStatus(s"Sent $amount to account number ${accountNumber.value}")
  }
  private class TestCreditCardService extends CreditCardService {
    override def processPayment(amount: BigDecimal, creditCard: CreditCard): PaymentStatus =
      PaymentStatus(s"Sent $amount to card number ${creditCard.cardNumber.value}")
  }
  private class TestCashService extends CashService {
    override def processPayment(amount: BigDecimal): PaymentStatus =
      PaymentStatus(s"Sent $amount in cash")
  }
}

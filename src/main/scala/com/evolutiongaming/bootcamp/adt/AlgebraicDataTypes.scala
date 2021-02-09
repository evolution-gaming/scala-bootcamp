package com.evolutiongaming.bootcamp.adt

object AlgebraicDataTypes {

  // ALGEBRAIC DATA TYPES

  // Algebraic Data Types or ADTs is a commonly used way of structuring data, used in many programming
  // languages (so this is not something unique to Scala).
  //
  // While the definition may sound scientific and complex, in reality we have already been using this
  // concept in `basics` package. For example, see `sealed trait Shape` in `ClassesAndTraits`. We will now
  // look into the concept and its use cases in more detail.

  // ADTs are widely used in Scala for multiple reasons:
  // - to ensure it is hard or even impossible to represent invalid data;
  // - because pattern matching and ADTs play nicely together.

  // Two common classes of ADTs are:
  // 1. product types: case classes and tuples;
  // 2. sum types: sealed traits and abstract classes.

  // PRODUCT TYPES

  // A product type allows to combine multiple values into one. Canonical Scala examples of product types are
  // case classes and tuples. See `Basics` and `ClassesAndTraits` for their introduction.

  // A product type is called like that because one can calculate how many different values it can possibly
  // have by multiplying the number of such possibilities for the types it combines. The resulting number
  // is called the arity of the product type.

  // Question. What is the arity of the product type `(Boolean, Boolean)`?
  type DoubleBoolean = (Boolean, Boolean)

  // Question. What is the arity of the product type `Person`?
  final case class Person(name: String, surname: String, age: Int)

  // Question. `Int`, `Double`, `String`, etc. are useful types from the Scala standard library, which can
  // represent a wide range of data. In the product type `Person`, both the name and the surname are
  // represented by `String`. Is that a good idea?

  // VALUE CLASSES

  // Value classes are a mechanism in Scala to avoid allocating runtime objects, while still providing
  // additional type safety. Runtime objects are not allocated in most cases, but there are notable
  // exceptions, see the following link for more details:
  // https://docs.scala-lang.org/overviews/core/value-classes.html

  // `Age` has a single, public val parameter that is the underlying runtime representation. The type at
  // compile time is `Age`, but at runtime, the representation is `Int`. Case classes can also be used to
  // define value classes, see `Name`.
  class Age(val value: Int) extends AnyVal
  final case class Name(value: String) extends AnyVal {
  // val drString = s"Dr. $value"
  }

  // Type aliases may seem similar to value classes, but they provide no additional type safety. They can,
  // however, increase readability of the code in certain scenarios.
  final case class Surname(value: String) extends AnyVal
  type SurnameAlias = String // No additional type safety in comparison to `String`, arguably a bad example!

  // Question. Can you come up with an example, where using type aliases would make sense?

  // Exercise. Rewrite the product type `Person`, so that it uses value classes.

  // SMART CONSTRUCTORS

  // Smart constructor is a pattern, which allows creating only valid instances of a class.

  // Exercise. Create a smart constructor for `GameLevel` that only permits levels from 1 to 80.
  final case class GameLevel private (value: Int) extends AnyVal
  object GameLevel {
    def create(value: Int): Option[GameLevel] = ???
  }

  // To disable creating case classes in any other way besides smart constructor, the following pattern
  // can be used. However, it is rather syntax-heavy and cannot be combined with value classes.
  sealed abstract case class Time private (hour: Int, minute: Int)
  object Time {
    def create(hour: Int, minute: Int): Either[String, Time] = Right(new Time(hour, minute) {})
  }

  // Exercise. Implement the smart constructor for `Time` that only permits values from 00:00 to 23:59 and
  // returns "Invalid hour value" or "Invalid minute value" strings in `Left` when appropriate.

  // Question. Is using `String` to represent `Left` a good idea?

  // SUM TYPES

  // A sum type is an enumerated type. To define it one needs to enumerate all its possible variants.
  // A custom boolean type `Bool` can serve as a canonical example.
  sealed trait Bool
  object Bool {
    final case object True extends Bool
    final case object False extends Bool
  }

  // Note that sealed keyword means that `Bool` can only be extended in the same file as its declaration.
  // Question. Why do you think sealed keyword is essential to define sum types?

  // A sum type is called like that because one can calculate how many different values it can possibly
  // have by adding the number of such possibilities for the types it enumerates. The resulting number
  // is called the arity of the sum type.

  // Question. What is the arity of the sum type `Bool`?

  // The power of sum and product types is unleashed when they are combined together. For example, consider a
  // case where multiple different payment methods need to be supported. (This is an illustrative example and
  // should not be considered complete.)
  final case class AccountNumber(value: String) extends AnyVal
  final case class CardNumber(value: String) extends AnyVal
  final case class ValidityDate(month: Int, year: Int)
  sealed trait PaymentMethod
  object PaymentMethod {
    final case class BankAccount(accountNumber: AccountNumber) extends PaymentMethod
    final case class CreditCard(cardNumber: CardNumber, validityDate: ValidityDate) extends PaymentMethod
    final case object Cash extends PaymentMethod
  }

  import PaymentMethod._

  final case class PaymentStatus(value: String) extends AnyVal
  trait BankAccountService {
    def processPayment(amount: BigDecimal, accountNumber: AccountNumber): PaymentStatus
  }
  trait CreditCardService {
    def processPayment(amount: BigDecimal, cardNumber: CreditCard): PaymentStatus
  }
  trait CashService {
    def processPayment(amount: BigDecimal): PaymentStatus
  }

  // Exercise. Implement `PaymentService.processPayment` using pattern matching and ADTs.
  class PaymentService(
    bankAccountService: BankAccountService,
    creditCardService: CreditCardService,
    cashService: CashService,
  ) {
    def processPayment(amount: BigDecimal, method: PaymentMethod): PaymentStatus = ???
  }

  // Let's compare that to `NaivePaymentService.processPayment` implementation, which does not use ADTs, but
  // provides roughly the same features as `PaymentService`.
  // Question. What are disadvantages of `NaivePaymentService`? Are there any advantages?
  trait NaivePaymentService { // Obviously a bad example!
    def processPayment(
      amount: BigDecimal,
      bankAccountNumber: Option[String],
      validCreditCardNumber: Option[String],
      isCash: Boolean,
    ): String = ???
  }

  // Exercise. Define an Algebraic Data Type `Car`, which has a manufacturer, a model, a production year,
  // and a license plate number (can contain from 3 to 8 upper case letters and numbers). Use value classes
  // and smart constructors as appropriate.
  type Car = Nothing

  // Homework. Define all algebraic data types, which would be needed to implement “Hold’em Hand Strength”
  // task you completed to join the bootcamp. Use your best judgement about particular data types to include
  // in the solution, you can model concepts like:
  //
  // 1. Suit
  // 2. Rank
  // 3. Card
  // 4. Hand (Texas or Omaha)
  // 5. Board
  // 6. Poker Combination (High Card, Pair, etc.)
  // 7. Test Case (Board & Hands to rank)
  // 8. Test Result (Hands ranked in a particular order for a particular Board, accounting for splits)
  //
  // Make sure the defined model protects against invalid data. Use value classes and smart constructors as
  // appropriate. Place the solution under `adt` package in your homework repository.

  // Attributions and useful links:
  // https://nrinaudo.github.io/scala-best-practices/definitions/adt.html
  // https://alvinalexander.com/scala/fp-book/algebraic-data-types-adts-in-scala/
  // https://en.wikipedia.org/wiki/Algebraic_data_type
}

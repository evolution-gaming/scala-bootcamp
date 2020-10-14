package com.evolutiongaming.bootcamp.error_handling

import scala.concurrent.Future
import scala.util.control.NonFatal

object ErrorHandling extends App {

  // ERROR HANDLING

  // Murphy's law states: "Anything that can go wrong will go wrong". This is applicable to every computer
  // program. Scala embraces Murphy's law and provides a number of ways to deal with its consequences
  // (i.e. handle errors).

  // When running in JVM, Scala uses Java exception class hierarchy, which extends `Throwable` superclass.
  //
  //               Throwable
  //                   |
  //     |---------------------------|
  //     |                           |
  //   Error                     Exception
  //
  // Error indicates a serious technical problem that an application should not even try to handle, e.g. JVM
  // is out of memory, there is a stack overflow, a method is not found, etc.
  //
  // Exception indicates a condition that an application may want to handle, e.g. business logic issue,
  // invalid data, temporary network glitch, etc. Note the "may" word: there are exceptions, which sometimes,
  // often or almost always should not be handled, e.g. `InterruptedException`, `NullPointerException`, etc.

  // In Java there are "checked" and "unchecked" exceptions. In Scala all exceptions are "unchecked", so we
  // will not cover this topic. If still interested, see:
  // https://howtodoinjava.com/java/exception-handling/checked-vs-unchecked-exceptions-in-java

  // A simple try-catch-finally block in Scala looks as follows. Note `NonFatal`, which is a handy extractor
  // of non-fatal throwables. It does not catch fatal errors and few other JVM- and Scala-specific `Throwable`
  // subtypes, which typically should not be handled.
  try {
    1 / 0
  } catch {
    case NonFatal(t) => println(t.getMessage)
  } finally {
    println("Will run no matter what")
  }

  // While throwing and catching exceptions is the default way of handling errors in Java, Scala (especially
  // functional Scala) prefers other approaches.

  // Question. Is this method safe to call? What can go wrong with it?
  def parseInt(string: String): Int = Integer.parseInt(string)

  // Question. When do you think throwing exceptions is a bad idea? When it is acceptable?

  // OPTION

  // Option is the simplest possible mechanism for handling errors in Scala. Use Option when only one thing
  // can go wrong or there is no interest in a particular reason for a failure.

  // Exercise. Implement `parseIntOption` method.
  def parseIntOption(string: String): Option[Int] = ???

  // The downside of Option is that it does not encode any information about what exactly went wrong. It only
  // states the mere fact that it did.

  // Question. Come up with few other examples where using Option for error handling seems a good idea.

  // EITHER & ADTs

  // Either is a more powerful alternative to Option. It not only encodes the fact that something went wrong,
  // but also provides means to carry a particular reason that has caused the issue.

  // Exercise. Implement `parseIntEither` method, returning the parsed integer as `Right` upon success and
  // "{{string}} does not contain an integer" as `Left` upon failure.
  def parseIntEither(string: String): Either[String, Int] = ???

  // As an alternative to `String`, a proper ADT can be introduced to formalize all error cases. As discussed
  // in `AlgebraicDataTypes` section, this provides a number of benefits, including an exhaustiveness check
  // at compile time, so one can be sure all error cases are handled.
  //
  // Note that this is a superficial example. Always think how detailed you want your error cases to be.
  sealed trait TransferError
  object TransferError {
    /** Returned when amount to credit is negative. */
    final case object NegativeAmount extends TransferError
    /** Returned when amount to credit is zero. */
    final case object ZeroAmount extends TransferError
    /** Returned when amount to credit is equal or greater than 1 000 000. */
    final case object AmountIsTooLarge extends TransferError
    /** Returned when amount to credit is within the valid range, but has more than 2 decimal places. */
    final case object TooManyDecimals extends TransferError
  }
  // Exercise. Implement `credit` method, returning `Unit` as `Right` upon success and the appropriate
  // `TransferError` as `Left` upon failure.
  def credit(amount: BigDecimal): Either[TransferError, Unit] = ???

  // `Either[Throwable, A]` is similar to `Try[A]`. However, because `Try[A]` has its error channel hardcoded
  // to a specific type and `Either[L, R]` does not, `Try[A]` provides more specific methods to deal with
  // throwables. So it may be easier to use in certain scenarios.
  //
  // Note that only non-fatal exceptions are caught by the combinators on Try. Serious system errors, on the
  // other hand, will be thrown. To distinguish between fatal and non-fatal exceptions, Try follows the same
  // principles as `NonFatal`, covered above.

  // VALIDATED

  // One limitation of all previously described approaches is that only the first encountered error is
  // reported to the caller. Sometimes it is fine. But there are cases, where the caller would want to see all
  // accumulated failures simultaneously.
  //
  // For example, when submitting a web form, a typical user would want to see all invalid fields right away.
  // Otherwise UX would struggle. The user would have to resubmit the form multiple times and only see one
  // particular error on each iteration. Here is where Validated from Cats library can help.

  final case class Student(username: String, age: Int)

  sealed trait ValidationError
  object ValidationError {
    final case object UsernameLengthIsInvalid extends ValidationError {
      override def toString: String = "Username must be between 3 and 30 characters"
    }
    final case object UsernameHasSpecialCharacters extends ValidationError {
      override def toString: String = "Username cannot contain special characters"
    }
    final case object AgeIsNotNumeric extends ValidationError {
      override def toString: String = "Age must be a number"
    }
    final case object AgeIsOutOfBounds extends ValidationError {
      override def toString: String = "Student must be of age 18 to 75"
    }
  }

  // There is a separate lecture about Cats library, so we will not go into details here. Suffice to say
  // we need few imports to bring the power of Cats to work with Validated data type.
  import cats.data.ValidatedNec
  import cats.syntax.all._

  object StudentValidator {

    import ValidationError._

    // `AllErrorsOr[A]` contains either non-empty Chain of validation errors or a value, if validation has
    // succeeded. It can be thought of as an error-accumulating version of Either.
    //
    // Chain is another data type from Cats library. It is similar to List, but supports both constant time
    // append and prepend (Scala's List offers only constant time prepend). Therefore it is a better fit for
    // usage with Validated, where errors are often accumulated by appending them.
    type AllErrorsOr[A] = ValidatedNec[ValidationError, A]

    private def validateUsername(username: String): AllErrorsOr[String] = {

      def validateUsernameLength: AllErrorsOr[String] =
        if (username.length >= 3 && username.length <= 30) username.validNec
        else UsernameLengthIsInvalid.invalidNec

      def validateUsernameContents: AllErrorsOr[String] =
        if (username.matches("^[a-zA-Z0-9]+$")) username.validNec
        else UsernameHasSpecialCharacters.invalidNec

      // `productR` method (can also be written as *>) accumulates both username related errors into a single
      // `AllErrorsOr[String]`. However, it ignores the result of the validator on the left and uses only the
      // result of the validator on the right (hence the `R` suffix).
      validateUsernameLength.productR(validateUsernameContents)
    }

    // Exercise. Implement `validateAge` method, so that it returns `AgeIsNotNumeric` if the age string is not
    // a number and `AgeIsOutOfBounds` if the age is not between 18 and 75. Otherwise the age should be
    // considered valid and returned inside `AllErrorsOr`.
    private def validateAge(age: String): AllErrorsOr[Int] = ???

    // `validate` method takes raw username and age values (for example, as received via POST request),
    // validates them, transforms as needed and returns `AllErrorsOr[Student]` as a result. `mapN` method
    // allows to map other N Validated instances at the same time.
    def validate(username: String, age: String): AllErrorsOr[Student] =
      (validateUsername(username), validateAge(age)).mapN(Student)
  }

  // HANDLING ERRORS IN A FUNCTIONAL WAY

  // 1. Method signatures should not lie about recoverable errors

  // Bad! We are promising the caller to return a JSON object for every string. This promise cannot be
  // fulfilled. This is a trivial example, but imagine a more obscure one. Sooner or later this method will
  // throw an exception instead of returning JSON. The caller may no be prepared for that.
  type Json = Any
  def parseJson(string: String): Json = ???

  // Good! The caller can immediately see we do not guarantee a JSON object for every string. Moreover, this
  // contact is embedded in the method signature. So the caller is forced to explicitly deal with the case,
  // where JSON is not produced. The end result: safer, more reliable program.
  def parseJsonOption(string: String): Option[Json] = ???

  // 2. Use the principle of least power when choosing error handling approach

  // "Given a choice of solutions, pick the least powerful solution capable of solving your problem." Scala is
  // an expressive language that usually provides multiple ways of solving the same problem. However, if you
  // decide to use the least powerful approach, you can manage complexity and make your code easier to
  // comprehend by others (and future self).

  // Least powerful to most powerful error handling techniques:
  // 1. Option
  // 2. Either & ADTs
  // 3. Try
  // 4. Validated

  // 3. Use separate channels for recoverable and non-recoverable errors

  // While it makes sense to be explicit about recoverable errors and bake them into method signatures,
  // non-recoverable errors should remain implicit. After all, they can happen at any point of execution of
  // our application and we can do nothing about them.

  // Question. Does the original `parseInt` method above adhere to this rule? What about `parseIntOption`,
  // `parseIntEither` and other methods we implemented in scope of this lecture?

  // The specified error handling rules also apply to Scala asynchronous effects, which are covered in later
  // lectures (see Asynchronous Programming and Asynchronous Effects). For example, ZIO data type has three
  // type parameters: `R` for environment, `E` for failure type and `A` for success type.
  sealed trait ZIO[-R, +E, +A]

  // As for asynchronous effects that do not offer separate channel for recoverable errors out of the box,
  // (see Future and Cats Effect IO), a common practice is to combine them with Option or Either to achieve
  // the same result.
  type FutureEither[E, A] = Future[Either[E, A]]

  // Homework. Place the solution under `error_handling` package in your homework repository.
  //
  // 1. Model `PaymentCard` class as an ADT (protect against invalid data as much as it makes sense).
  // 2. Add `ValidationError` cases (at least 5, may be more).
  // 3. Implement `validate` method to construct `PaymentCard` instance from the supplied raw data.
  object Homework {

    case class PaymentCard(/* Add parameters as needed */)

    sealed trait ValidationError
    object ValidationError {
      ??? // Add errors as needed
    }

    object PaymentCardValidator {

      type AllErrorsOr[A] = ValidatedNec[ValidationError, A]

      def validate(
        name: String,
        number: String,
        expirationDate: String,
        securityCode: String,
      ): AllErrorsOr[PaymentCard] = ???
    }
  }

  // Attributions and useful links:
  // https://www.lihaoyi.com/post/StrategicScalaStylePrincipleofLeastPower.html#error-handling
  // https://www.geeksforgeeks.org/scala-exception-handling/
  // https://typelevel.org/cats/datatypes/validated.html
  // https://blog.ssanj.net/posts/2019-08-18-using-validated-for-error-accumulation-in-scala-with-cats.html
}

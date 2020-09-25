package com.evolutiongaming.bootcamp.error_handling

import scala.util.Try
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
  // an expressive language, that usually provides multiple ways of solving the same problem. However, if you
  // decide to use the least powerful approach, you can manage complexity and make your code easier to
  // comprehend by others (and future self).

  // Least powerful to most powerful error handling techniques:
  // 1. Option
  // 2. Either & ADTs
  // 3. Throwable

  // Question. Why throwables are more powerful than ADTs?

  // 3. Use separate channels for recoverable and non-recoverable errors

  // While it makes sense to be explicit about recoverable errors and bake them into method signatures,
  // non-recoverable errors should remain implicit. After all, they can happen at any point of execution of
  // our application and we can do nothing about them.

  // Attributions and useful links:
  // https://www.lihaoyi.com/post/StrategicScalaStylePrincipleofLeastPower.html#error-handling
  // https://www.geeksforgeeks.org/scala-exception-handling/
}

package com.evolutiongaming.bootcamp.error_handling

import scala.util.control.NonFatal

// https://www.geeksforgeeks.org/scala-exception-handling/
object Basics {

  // An exception is an unwanted or unexpected event, which occurs during the execution of a program i.e at run time

  def parseInt(string: String): Int = try {
    Integer.parseInt(string)
  } catch {
    case error: NumberFormatException =>
      error.printStackTrace(System.err)
      throw error
  } finally {
    println("Hello there")
  }

  // Hierarchy
  /*
                               Throwable
                                   |
                     |------------------------------|
                     |                              |
                 Exception                        Error
                     |
              RuntimeException

    Errors are `unchecked` and denote some critical conditions (OutOfMemory, StackOverflow, MethodNotFound, etc)
    Descendants of RuntimeExceptions are `unchecked` – you don't need to declare them in method signature
    All other subtypes of Exception are 'checked' exceptions in Java:

    class MyCheckedException extends Exception {
      public MyCheckedException(String msg) {
        super(msg);
      }
    }

    public int stringToInt(String string) throws MyCheckedException {
      try {
        int n = Integer.parseInt(string);
        if (n > 0) return n;
        else throw new MyCheckedException("Only positive values are supported");
      } catch (NumberFormatException e) {
        throw new MyCheckedException(e.getMessage());
      }
    }
  */


  // In Scala though there are no checked exceptions and this compiles
  final class MyException(msg: String) extends Exception(msg)

  def stringToPositiveInt(str: String): Int = try {
    val n = Integer.parseInt(str)
    if (n > 0) n
    else throw new MyException("Only positive values are supported")
  } catch {
    case e: NumberFormatException => throw new MyException(e.getMessage)
  }



  // Why there are no checked exceptions in Scala?

  // Non-composable
  def firstMethod(n: Int): Boolean /* throws MyException1 */ = ???
  def secondMethod(str: String): Int /* throws MyException2 */ = ???
  def logic(): Boolean /* throws Exception */ = firstMethod(secondMethod("Hello, World"))

  // Don't play nicely with lambdas and FP
  List("test").map(str => firstMethod(secondMethod(str))) // throws what?

  // Rarely denote recoverable errors in reality (hi IOException!)
  // Recoverable errors are errors conditions which you can handle with your business logic
  // Non-recoverable errors are errors for which you can't do much (except some kind of logging)



  // To match critical errors Scala has its own mechanism instead of Error supertype
  def memoryHoggingFunc(): Unit = throw new OutOfMemoryError("Catch Me If You Can")

  // Bad!
  def catchingFatalErrors(): Unit = try {
    memoryHoggingFunc()
  } catch {
    case error: Throwable => println(error.getMessage)
  }

  // Good! :3
  def tryOrDie(): Unit = try {
    memoryHoggingFunc()
  } catch {
    case NonFatal(error) => println(error.getMessage)
  }



  // Data types for recoverable errors:
  // Either[E, A]
  // Option[A] <~> Either[Unit, A]

  // Data types for non-recoverable errors (T[A] <~> Either[Throwable, A])
  // Try[A]
  // Future[A]
  // IO[A] (yet undiscovered)

  // Often you may see these combined, e.g. Future[Option[User]]

  /* Have some common "shape":
    - can represent successful result
    - can represent errors
    - can be mapped over: F[A].map(A => B) -> F[B]
    - can be flatMapped: F[A].flatMap(A => F[B]) -> F[B] (with short circuiting)
   */
}

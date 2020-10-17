package com.evolutiongaming.bootcamp.asynchronous_effects

import cats.effect.{ExitCode, IO, IOApp}

import scala.io.StdIn
import cats.implicits._

import scala.annotation.tailrec
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

/*
 * Side effects - Modifying or accessing shared state outside the local environment - producing an
 *                observable effect other than returning a value to the invoker.
 *
 *                For example - updating a global variable, writing/reading to disk/console.
 *
 * But don't we need to do these "side effects" to write useful programs?
 *
 * Yes, indeed we do.
 *
 * In functional programming we wrap side effects into "IO Monads":
 *  - This turns (or captures, or encodes) them into immutable data (pure values)
 *  - Keeps referential transparency so it is easier to refactor our programs and reason about them
 *  - We can evaluate them when we want
 *  - Can be sequentially executed
 *  - Can be used in `for`-comprehensions
 *  - Can model complex series of concurrent computations
 *
 * There are a number of popular libraries providing IO Monads in the Scala ecosystem, each with their
 * pros & cons:
 *  - Cats Effect - https://typelevel.org/cats-effect/ - versions 2 and 3
 *  - ZIO - https://zio.dev/
 *  - Monix - https://monix.io/
 *
 *  There are also ways how to write software without being tied to a particular IO Monad ("Tagless Final"
 *  pattern), but we will not discuss this in this lecture.
 *
 * Asynchronous Effects, as opposed to Scala Future-s, are lazy. Nothing is run until an "unsafe" method
 * is executed (by your code, or by the IOApp trait) - usually at the "end of the world".
 *
 * The IO monad in Cats Effect is called `IO`.
 *
 * A value of type IO[A] is a computation which, when evaluated, can perform effects before returning a value
 * of type A.
 *
 * It is a data structure that represents a description of a side effecting computation.
 *
 * It describes synchronous or asynchronous computations that:
 * - On evaluation yield exactly one result
 * - Can end in either success or failure (in case of failure flatMap chains get short-circuited)
 * - Can be canceled (if the user provides cancellation logic)
 */

/*
 * Before running this program, try to guess what the output will be.
 */
object LazyVsEagerApp extends App {
  import concurrent.ExecutionContext.Implicits.global

  private def doTaskFuture(x: String): Future[Unit] = Future {
    println(s"Doing task $x")
  }

  val future1: Future[Unit] = for {
    _ <- doTaskFuture("future1")
    _ <- doTaskFuture("future1")
  } yield ()

  Await.result(future1, Duration.Inf)

  val taskFuture  = doTaskFuture("future2")
  val future2: Future[Unit] = for {
    _ <- taskFuture
    _ <- taskFuture
  } yield ()

  Await.result(future2, Duration.Inf)

  val io1 = for {
    _ <- doTaskIO("io1")
    _ <- doTaskIO("io1")
  } yield ()

  io1.unsafeRunSync()

  private def doTaskIO(x: String): IO[Unit] = IO {
    println(s"Doing task $x")
  }

  val task = doTaskIO("io2")
  val io2 = for {
    _ <- task
    _ <- task
  } yield ()

  io2.unsafeRunSync()
}

/*
 * Question. Which approach will be easier to refactor and reason about?
 *
 * Question. Can you explain the concept of "referential transparency" using the code above?
 */

object IOBuildingBlocks {
  // TODO:
  /*
   * `IO.pure` lifts pure values into IO, yielding IO values that are "already evaluated".
   * It's eagerly evaluated therefore don't pass side effecting computations into it.
   *
   * `IO.unit` is just `IO.pure(())`, commonly used to signal completion of side effecting routines.
   *
   * `IO.apply` describes operations that can be evaluated immediately, on the current thread.
   */
  def putStrLn(value: String): IO[Unit] = IO(println(value))
  val readLn: IO[String] = IO(StdIn.readLine())

  for {
    _ <- putStrLn("What's your name?")
    n <- readLn
    _ <- putStrLn(s"Hello, $n!")
  } yield ()

  // TODO: more from https://typelevel.org/cats-effect/datatypes/io.html, including:
  // Raising errors & recovering from them
  // Launching parallel IOs and collecting results, sequence, parSequence
  // Cancellation & infinite running
  // Forking using IO.start
}

object Exercise1_Imperative {
  import com.evolutiongaming.bootcamp.asynchronous_effects.Exercise1_Common.response
  private var counter: Int = 0

  @tailrec
  def main(args: Array[String]): Unit = {
    println("What is your favourite animal?")
    val animal = StdIn.readLine()
    val output = response(animal)
    output match {
      case Some(x)  =>
        println(x)

      case None     =>
        if (counter >= 2) {
          println("I am disappoint. You have failed to answer too many times.")
          sys.exit(1)
        } else {
          counter += 1
          println("Empty input is not valid, try again...")
          main(args)
        }
    }
  }

  // Question: How do you test this?
  // Question: How do you refactor this to retry upon empty input at most 3 times?
}

object Exercise1_Common {
  def response(animal: String): Option[String] = animal.trim match {
    case "cat" | "cats"   =>  "In ancient times cats were worshipped as gods; they have not forgotten this.".some
    case "dog" | "dogs"   =>  "Be the person your dog thinks you are.".some
    case x if x.nonEmpty  =>  s"I don't know what to say about '$x'.".some
    case _                =>  none
  }

  trait Console {
    def writeString(value: String): IO[Unit]
    def readString: IO[String]
  }

  /*
   * Exercise 1a. Implement `RealConsole` using `IO.apply` and `IO.unit`.
   */
  final object RealConsole extends Console {
    // TODO: replace `writeString` implementation with `???` after code review
    def writeString(value: String): IO[Unit] = IO(println(value))
    // TODO: replace `readString` implementation with `???` after code review
    def readString: IO[String] = IO(StdIn.readLine())
  }
}

/*
 * Exercise 1b. Re-implement Exercise1_Imperative avoiding side-effecting code using the "IO Monad"
 *
 * Using the following can be helpful:
 *  - `for`-comprehension
 *  - `IO.as` as a `map` which discards the first result to return `ExitCode`-s
 *  - `*>` as a `flatMap` which discards the first result to sequence `IO[Unit]` with another `IO`
 *  - Tests in `AsynchronousEffectsSpec` to check your work
 */
object Exercise1_Functional extends IOApp {
  import Exercise1_Common._

  // TODO: replace `process` implementation `???` after code review
  def process(console: Console, counter: Int = 0): IO[ExitCode] = {
    import console._

    for {
      _           <-  writeString("What is your favourite animal?")
      animal      <-  readString
      output      =   response(animal)
      result      <-  output match {
                        case None =>
                          if (counter >= 2) {
                            writeString("I am disappoint. You have failed to answer too many times.") as ExitCode.Error
                          } else {
                            writeString("Empty input is not valid, try again...") *> process(console, counter + 1)
                          }

                        case Some(x) =>
                          writeString(x) as ExitCode.Success
                      }
    } yield result
  }

  override def run(args: List[String]): IO[ExitCode] = process(RealConsole)
}

/*
 * Provide your own simple implementation of `IO` along with tests that check that it works correctly.
 */
object Homework {
  // TODO - fill this out, then do a reference implementation and tests
  final class IO[A] {
    def map[B](f: A => B): IO[B] = ???
    def flatMap[B](f: A => IO[B]): IO[B] = ???
  }

  object IO {
    def pure[A](a: A): IO[A] = ???
    val unit: IO[Unit] = ???

    def apply[A](body: => A): IO[A] = ???
  }
}

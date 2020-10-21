package com.evolutiongaming.bootcamp.effects

import java.util.concurrent.atomic.AtomicBoolean

import cats.effect.{ExitCode, IO, IOApp}

import scala.io.StdIn
import cats.implicits._

import scala.annotation.tailrec
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.{Random, Try}

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

/*
 * `IO.pure` lifts pure values into IO, yielding IO values that are "already evaluated".
 * It's eagerly evaluated therefore don't pass side effecting computations into it.
 *
 * `IO.unit` is just `IO.pure(())`, commonly used to signal completion of side effecting routines.
 *
 * `IO.apply` describes operations that can be evaluated immediately, on the current thread.
 */

trait Console {
  def putStrLn(value: String): IO[Unit]
  def readStrLn: IO[String]
}

object Console {
  object Real extends Console {
    def putStrLn(value: String): IO[Unit] = IO(println(value))
    def readStrLn: IO[String] = IO(StdIn.readLine())
  }
}

import Console.Real._

/*
 * `IO` is a Monad and thus you can work with it as you would with other Monad-s - use `.map`, `.flatMap`,
 * and `for`-comprehensions.
 */
object IOBuildingBlocks1 extends IOApp {
  private val nameProgram = for {
    _ <- putStrLn("What's your name?")
    n <- readStrLn
    _ <- putStrLn(s"Hello, $n!")
  } yield ()

  def run(args: List[String]): IO[ExitCode] = nameProgram as ExitCode.Success
}

object Exercise1_Imperative {
  import com.evolutiongaming.bootcamp.effects.Exercise1_Common.response
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
}

/*
 * Exercise 1. Re-implement Exercise1_Imperative avoiding side-effecting code using the "IO Monad"
 *
 * Using the following can be helpful:
 *  - `for`-comprehension
 *  - `IO#as` as a `map` which discards the first result to return `ExitCode`-s
 *  - `*>` as a `flatMap` which discards the first result to sequence `IO[Unit]` with another `IO`
 *  - Tests in `AsynchronousEffectsSpec` to check your work
 */
object Exercise1_Functional extends IOApp {
  import Exercise1_Common._

  // TODO: replace `process` implementation `???` after code review
  def process(console: Console, counter: Int = 0): IO[ExitCode] = {
    import console._

    for {
      _           <-  putStrLn("What is your favourite animal?")
      animal      <-  readStrLn
      output      =   response(animal)
      result      <-  output match {
                        case None =>
                          if (counter >= 2) {
                            putStrLn("I am disappoint. You have failed to answer too many times.") as ExitCode.Error
                          } else {
                            putStrLn("Empty input is not valid, try again...") *> process(console, counter + 1)
                          }

                        case Some(x) =>
                          putStrLn(x) as ExitCode.Success
                      }
    } yield result
  }

  override def run(args: List[String]): IO[ExitCode] = process(Console.Real)
}

object IOBuildingBlocks2 extends IOApp {
  import scala.concurrent.ExecutionContext.Implicits.global

  /* Asynchronous process - a process which continues its execution in a different place or time than the one
   * that started it.
   *
   * Concurrency - a program structuring technique in which there are multiple logical threads of control,
   * whose effects are interleaved.
   *
   * IO.async - describes an asynchronous process which cannot be cancelled
   */
  private def tickNSeconds(n: Int): IO[Unit] =
    if (n <= 0)
      IO.unit
    else
      for {
        _ <- putStrLn(s"Tick $n")
        _ <- IO.sleep(1.second)
        _ <- tickNSeconds(n - 1)
      } yield ()

  private val asyncProgram = for {
    _ <- putStrLn("launching async")
    _ <- IO.async[Unit] { cb: (Either[Throwable, Unit] => Unit) =>
      tickNSeconds(2)
        .unsafeToFuture() // avoid 'unsafe*' in real code
        .onComplete { x: Try[Unit] =>
          cb(x.toEither)
        }
    }
    _ <- putStrLn("async finished")
  } yield ()

  /*
   * Cancellation is the ability to interrupt an IO task before the completion. You should make sure you
   * release any acquired resources.
   *
   * IO.cancelable - similar to IO.async, but should return an IO which captures the cancellation logic.
   *
   * `IO#start` - forks a new IO as a Fiber (you can think of them as lightweight threads). Fibers can be
   * `join`-ed (awaiting the result) or `cancel`-ed.
   */
  private val cancelableProgram1 = for {
    _ <- putStrLn("Launching cancelable")
    io = IO.cancelable[Int] { _ =>
      val keepGoing = new AtomicBoolean(true)

      scala.concurrent.ExecutionContext.global.execute { () =>
        while (keepGoing.get()) {
          Thread.sleep(1000)
          println("Tick")
        }
      }

      IO { keepGoing.getAndSet(false) }.void
    }
    fiber <- io.start
    _ <- putStrLn(s"Started $fiber")
    _ <- IO.sleep(5.seconds)
    _ <- putStrLn(s"cancelling $fiber...")
    _ <- fiber.cancel
    _ <- putStrLn(s"cancelable $fiber finished")
  } yield ()

  private val cancelableProgram2 = for {
    fiber <- tickNSeconds(60).start
    _ <- putStrLn(s"started $fiber...")
    _ <- IO.sleep(5.seconds)
    _ <- putStrLn(s"cancelling $fiber...")
    _ <- fiber.cancel
    _ <- putStrLn(s"cancelable $fiber finished...")
  } yield ()

  /*
   * `IO.suspend` is equivalent to `IO(f).flatten` and can be used to introduce an async boundary for
   * "trampolining" to avoid a stack overflow.
   *
   *  def suspend[A](thunk: => IO[A]): IO[A]
   *
   * IO.flatMap is "trampolined" (that means - it is stack-safe).
   *
   * Question: What happens when `fib` is executed with a large enough `n`?
   * Question: How can we fix it using `IO.suspend`?
   */
  private def fib(n: Int, a: Long, b: Long): IO[Long] = {
    if (n > 0) fib(n - 1, b, a + b).map(_ + 0) // Question: Why did I add this useless `.map` here?
    else IO.pure(a)
  }

  private def suspendProgram: IO[Unit] = fib(100000, 0, 1).map(_.toString).flatMap(putStrLn)

  /*
   * `sequence`     -   takes a list of `IO`, executes them in sequence and returns an `IO` with a collection
   *                    of all the results.
   *
   * `parSequence`  -   does the same, but executes in parallel
   */
  private val tasks: List[IO[Int]] = (0 to 10)
    .map { x =>
      IO.sleep(Random.nextInt(1000).millis) *> putStrLn(x.toString) as x
    }
    .toList

  private val sequenceProgram: IO[Unit] = for {
    _         <-  putStrLn("start sequence")
    sequenced <-  tasks.sequence
    _         <-  putStrLn(s"end sequence, results: $sequenced")
  } yield ()

  private val parSequenceProgram: IO[Unit] = for {
    _         <-  putStrLn("start parSequence")
    sequenced <-  tasks.parSequence
    _         <-  putStrLn(s"end parSequence, results: $sequenced")
  } yield ()

  // TODO: keep going - https://typelevel.org/cats-effect/datatypes/io.html
  // ContextShift
  // Raising errors & recovering from them
  // Resources

  def run(args: List[String]): IO[ExitCode] = for {
    _ <- asyncProgram
    _ <- cancelableProgram1
    _ <- cancelableProgram2
    _ <- suspendProgram
    _ <- sequenceProgram
    _ <- parSequenceProgram
  } yield ExitCode.Success
}

/*
 * Provide your own "crude" implementation of a subset of `IO` functionality.
 *
 * Provide also tests for these implementations in EffectsHomeworkSpec.
 *
 * Refer to:
 *  - https://typelevel.org/cats-effect/datatypes/io.html
 *  - https://typelevel.org/cats-effect/api/cats/effect/IO$.html
 *  - https://typelevel.org/cats-effect/api/cats/effect/IO.html
 * about the meaning of each method as needed.
 */
object EffectsHomework {
  // TODO - do a reference implementation and reference tests, then remove
  final class IO[A] {
    def map[B](f: A => B): IO[B] = ???
    def flatMap[B](f: A => IO[B]): IO[B] = ???
    def *>[B](another: IO[B]): IO[B] = ???
    def as[B](newValue: => B): IO[B] = ???
    def void: IO[Unit] = ???
    def attempt: IO[Either[Throwable, A]] = ???
    def option: IO[Option[A]] = ???
    def handleErrorWith[AA >: A](f: Throwable => IO[AA]): IO[AA] = ???
    def redeem[B](recover: Throwable => B, map: A => B): IO[B] = ???
    def redeemWith[B](recover: Throwable => IO[B], bind: A => IO[B]): IO[B] = ???
    def unsafeRunSync(): A = ???
    def unsafeToFuture(): Future[A] = ???
  }

  object IO {
    def apply[A](body: => A): IO[A] = ???
    def suspend[A](thunk: => IO[A]): IO[A] = ???
    def delay[A](body: => A): IO[A] = ???
    def pure[A](a: A): IO[A] = ???
    def fromEither[A](e: Either[Throwable, A]): IO[A] = ???
    def fromOption[A](option: Option[A])(orElse: => Throwable): IO[A] = ???
    def fromTry[A](t: Try[A]): IO[A] = ???
    def none[A]: IO[Option[A]] = ???
    def raiseError[A](e: Throwable): IO[A] = ???
    def raiseUnless(cond: Boolean)(e: => Throwable): IO[Unit] = ???
    def raiseWhen(cond: Boolean)(e: => Throwable): IO[Unit] = ???
    def unlessA(cond: Boolean)(action: => IO[Unit]): IO[Unit] = ???
    def whenA(cond: Boolean)(action: => IO[Unit]): IO[Unit] = ???
    val unit: IO[Unit] = ???
  }
}

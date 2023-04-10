package com.evolutiongaming.bootcamp.effects.v3

import cats.Monad
import cats.effect.{Async, ExitCode, IO, IOApp, Sync}
import cats.implicits.none
import cats.syntax.all._

import java.util.concurrent.Executors
import scala.annotation.tailrec
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.io.StdIn
import scala.util.control.NoStackTrace
import scala.util.{Random, Try}

/*
 * Side effects - Modifying or accessing shared state outside the local environment - producing an
 *                observable effect other than returning a value to the invoker.
 *
 *                For example - updating a global variable, reading/writing to disk/console.
 *
 * But don't we need to do these "side effects" to write useful programs?
 *
 * Yes, indeed we do.
 *
 * In functional programming we wrap side effects into "IO Monads":
 *  - This turns (or captures, or encodes) them into immutable data (pure values)
 *  - Keeps referential transparency so that it is easier to refactor our programs and reason about them
 *  - We can evaluate them when we want
 *  - They can be executed sequentially or in parallel
 *  - They can be used in `for`-comprehensions
 *  - They can model complex series of concurrent computations
 *
 * There are a number of popular libraries providing IO Monads in the Scala ecosystem, each with their
 * pros & cons:
 *  - Cats Effect - https://typelevel.org/cats-effect/ - versions 2 and 3
 *  - ZIO - https://zio.dev/
 *  - Monix - https://monix.io/
 *
 *  There are also ways how to write software without being tied to a particular IO Monad ("Tagless Final"
 *  pattern)
 *
 * Asynchronous Effects, as opposed to Scala `Future`-s, are lazy. Nothing is run until an "unsafe" method
 * is executed (by your code, or by the `IOApp` trait) - usually at the "end of the world".
 *
 * The IO Monad in Cats Effect is called `IO`.
 *
 * A value of type IO[A] is a computation which, when evaluated, can perform effects before returning a value
 * of type A.
 *
 * It is a data structure that represents a description of a side effecting computation.
 *
 * It describes synchronous or asynchronous computations that:
 * - On evaluation yield exactly one result
 * - Can end in either success or failure (and in case of failure, the `flatMap` chains get short-circuited)
 * - Can be canceled (if the user provides cancellation logic)
 */

/*
 * Before running this program, try to guess what the output will be.
 */
object LazyVsEagerApp extends App {
  import concurrent.ExecutionContext.Implicits.global
  import cats.effect.unsafe.implicits.{global => globalRuntime}

  private def doTaskFuture(x: String): Future[Unit] =
    Future { println(s"Doing task $x") }

  val future1: Future[Unit] = for {
    _ <- doTaskFuture("future1")
    _ <- doTaskFuture("future1")
  } yield ()

  Await.result(future1, Duration.Inf)

  println("-" * 100)

  val taskFuture            = doTaskFuture("future2")
  val future2: Future[Unit] = for {
    _ <- taskFuture
    _ <- taskFuture
  } yield ()

  Await.result(future2, Duration.Inf)

  println("-" * 100)

  private def doTaskIO(x: String): IO[Unit] =
    IO { println(s"Doing task $x") }

  val io1 = for {
    _ <- doTaskIO("io1")
    _ <- doTaskIO("io1")
  } yield ()

  io1.unsafeRunSync()

  println("-" * 100)

  val task = doTaskIO("io2")
  val io2  = for {
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

object ConsoleSimple {
  def readString(): String          = StdIn.readLine()
  def putString(text: String): Unit = println(text)
}

object HelloWorldSimple extends App {
  import ConsoleSimple._

  putString("What is your name: ")
  val name = readString()
  putString(s"Hello, $name!\n")
}

/* Some basic building blocks:
 *
 * `IO.pure` lifts pure values into IO, yielding IO values that are "already evaluated".
 *
 * It's eagerly evaluated therefore don't pass side effecting computations into it.
 *
 * `IO.unit` is just `IO.pure(())`, commonly used to signal completion of side effecting routines.
 *
 * `IO.apply` delays the execution of the passed computation to some time in future
 */

trait Console {
  def putString(value: String): IO[Unit]
  def readString: IO[String]
}

object ConsoleIO extends Console {
  def putString(value: String): IO[Unit] = IO(println(value))
  def readString: IO[String]             = IO(StdIn.readLine())
}

import com.evolutiongaming.bootcamp.effects.v3.ConsoleIO._

/*
 * `IO` is a Monad and thus you can work with it as you would with other Monad-s, for example,
 * use `.map`, `.flatMap`, and `for`-comprehensions.
 *
 * `IOApp` is the `App` equivalent for `IO`-based programs and serves as the "end of the world" where
 * effects are run.
 */
object HelloWorldIO extends IOApp {

  private val nameProgram = for {
    _    <- putString("What's your name?")
    name <- readString
    _    <- putString(s"Hi, $name!")
  } yield ()

  def run(args: List[String]): IO[ExitCode] =
    nameProgram.as(ExitCode.Success)
}

object HelloWorldTF extends IOApp {
  def run(args: List[String]): IO[ExitCode] = ???
}

object Exercise1_Common {
  def response(animal: String): Option[String] =
    animal.trim match {
      case "cat" | "cats"  => "In ancient times cats were worshipped as gods; they have not forgotten this.".some
      case "dog" | "dogs"  => "Be the person your dog thinks you are.".some
      case x if x.nonEmpty => s"I don't know what to say about '$x'.".some
      case _               => none
    }
}

object Exercise1_Imperative {
  import Exercise1_Common.response

  private var counter: Int = 0

  @tailrec
  def main(args: Array[String]): Unit = {
    println("What is your favourite animal?")
    val animal = StdIn.readLine()
    val output = response(animal)
    output match {
      case Some(x) =>
        println(x)

      case None =>
        if (counter >= 2) {
          println("I am disappointed. You have failed to answer too many times.")
          sys.exit(1)
        } else {
          counter += 1
          println("Empty input is not valid, try again...")
          main(args)
        }
    }
  }

  // Question: How do you test this?
}

/*
 * Exercise 1. Re-implement Exercise1_Imperative avoiding side-effecting code using the "IO Monad"
 *
 * Using the following can be helpful:
 *  - `for`-comprehension
 *  - `IO#as` as a `map` which discards the first result to return `ExitCode`-s
 *  - `*>` as a `flatMap` which discards the first result to sequence `IO[Unit]` with another `IO`
 *  - Tests in `EffectsSpec` to check your work
 */
object Exercise1_Functional extends IOApp {

  def process(console: Console, counter: Int = 0): IO[ExitCode] = ???

  def run(args: List[String]): IO[ExitCode] = process(ConsoleIO)
}

/*
 * `IO.defer` is equivalent to `IO(f).flatten` and can be used to avoid a stack overflow.
 *
 *   def suspend[A](thunk: => IO[A]): IO[A]
 *
 * `IO.flatMap` is also "trampolined" (that means - it is stack-safe).
 *
 * Question: What happens when `fib` is executed with a large enough `n`?
 * Question: How can we fix it using `IO.defer`?
 */
object SuspendApp extends IOApp {

  private def fib(
    n: Int,
    a: Long = 0,
    b: Long = 1,
  ): IO[Long] =
    n match {
      case 0 => IO.pure(a)
      case _ => fib(n - 1, b, a + b).map(_ + 0) // Question: Why did I add this useless `.map` here?
    }

  def run(args: List[String]): IO[ExitCode] =
    fib(100000)
      .flatMap(x => putString(s"fib = $x"))
      .as(ExitCode.Success)
}

/*
 * `sequence`     -   takes a list of `IO`, executes them in sequence and returns an `IO` with a collection
 *                    of all the results.
 *
 * `parSequence`  -   does the same, but executes in parallel
 */
object Sequence extends IOApp {

  private val listOfInts = (1 to 10).toList

  def printWithDelayAndReturn[A](a: A): IO[A] =
    IO
      .sleep(Random.nextInt(1000).millis)
      .flatMap(_ => putString(a.toString))
      .as(a)

  private val tasks: List[IO[Int]] =
    listOfInts.map(printWithDelayAndReturn)

  private val sequenceProgram: IO[Unit] =
    for {
      _         <- putString("start sequence")
      sequenced <- tasks.sequence
      _         <- putString(s"end sequence, results: $sequenced")
    } yield ()

  private val parSequenceProgram: IO[Unit] =
    for {
      _         <- putString("start parSequence")
      sequenced <- tasks.parUnorderedSequence
      _         <- putString(s"end parSequence, results: $sequenced")
    } yield ()

  def run(args: List[String]): IO[ExitCode] =
    for {
      _ <- sequenceProgram
      _ <- parSequenceProgram
    } yield ExitCode.Success
}

/*
 * Handling errors - operations available for `MonadError` and `ApplicativeError` are available for `IO`.
 *
 * See:
 *  - https://typelevel.org/cats/api/cats/MonadError.html
 */
object ErrorsHandling extends IOApp {

  private def failingProgram: IO[String] =
    for {
      // `raiseError` fails the `IO` with the specified exception
      _ <- IO.raiseError { new RuntimeException("errorMessage") }
    } yield "success"

  def run(args: List[String]): IO[ExitCode] =
    for {
      attempt <- failingProgram.attempt // Either[Throwable, A]
      _       <- putString(s"attempt = $attempt")

      option <- failingProgram.option // Option[A]
      _      <- putString(s"option = $option")

      handleError <- failingProgram.handleError(x => s"error:  ${x.getMessage}")
      _           <- putString(s"handleError = $handleError")

      handleErrorWith <- failingProgram.handleErrorWith(x => IO.pure(s"error: ${x.getMessage}"))
      _               <- putString(s"handleErrorWith = $handleErrorWith")

      recover <- failingProgram.recover {
        case x if x.getMessage == "errorMessage" => s"error: ${x.getMessage}"
      }
      _       <- putString(s"recover = $recover")

      recoverWith <- failingProgram.recoverWith {
        case x if x.getMessage == "errorMessage" => IO.pure(s"error: ${x.getMessage}")
      }
      _           <- putString(s"recoverWith = $recoverWith")

      redeem <- failingProgram.redeem(
        (x: Throwable) => s"error: ${x.getMessage}",
        (x: String) => s"success: $x",
      )
      _      <- putString(s"redeem = $redeem")

      redeemWith <- failingProgram.redeemWith(
        (x: Throwable) => IO.pure(s"error: ${x.getMessage}"),
        (x: String) => IO.pure(s"success: $x"),
      )
      _          <- putString(s"redeemWith = $redeemWith")
    } yield ExitCode.Success
}

object ErrorsExercise extends IOApp {

  trait Console[F[_]] {
    def readString: F[String]
    def putString(text: String): F[Unit]
  }

  object Console {
    def apply[F[_]: Sync]: Console[F] =
      new Console[F] {
        def readString: F[String]            = Sync[F].delay(StdIn.readLine())
        def putString(text: String): F[Unit] = Sync[F].delay(println(text))
      }
  }

  sealed trait ValidationError extends Throwable with NoStackTrace

  object ValidationError {

    final case class InvalidAge(age: Int) extends ValidationError {
      override def getMessage: String = s"'$age' is invalid age"
    }

    final case class InvalidName(name: String) extends ValidationError {
      override def getMessage: String = s"'$name' is invalid name"
    }
  }

  sealed abstract case class Age private (value: Int) {
    override def toString: String = value.toString
  }

  object Age {
    def from(value: Int): Either[ValidationError, Age] =
      Either.cond(value > 0, new Age(value) {}, ValidationError.InvalidAge(value))
  }

  sealed abstract case class Name private (value: String) {
    override def toString: String = value
  }

  object Name {
    def from(value: String): Either[ValidationError, Name] =
      Either.cond(value.nonEmpty, new Name(value) {}, ValidationError.InvalidName(value))
  }

  final case class Person(name: Name, age: Age) {
    override def toString: String = s"This is $name and he is $age years old"
  }

  def readPerson[F[_]](console: Console[F]): F[Person] = {

    val readName: F[Name] = ???

    val readAge: F[Age] = ???

    ???
  }

  def program[F[_]: Monad](console: Console[F]): F[Unit] = ??? // read person and print it

  def run(args: List[String]): IO[ExitCode] =
    program(Console[IO]).as(ExitCode.Success)
}

/* `IO` can describe asynchronous processes via the `IO.async` and `IO.cancelable` builders.
 *
 * Asynchronous process - a process which continues its execution in a different place or time than the one
 * that started it.
 *
 * Concurrency - a program structuring technique in which there are multiple logical threads of control,
 * whose effects are interleaved.
 *
 * `IO.async` can describe simple asynchronous processes that cannot be canceled.
 * */
object AsyncApp extends IOApp {

  private val ec = Executors.newFixedThreadPool(4)

  def requestAndChangeThread(callback: Either[Throwable, Int] => Unit): Unit = {
    val runnable: Runnable = () => {
      println(s"Thread from pool: ${Thread.currentThread().getName}")
      callback(getGoogleStatus())
    }

    ec.execute(runnable)
  }

  def requestF[F[_]: Async]: F[Int] =
    Async[F].async_ { cb =>
      println(s"Starting async: ${Thread.currentThread().getName}")
//      cb(getGoogleStatus())
      requestAndChangeThread(cb)
    }

  override def run(args: List[String]): IO[ExitCode] = {
    val app = for {
      _        <- IO(println(s"Starting run: ${Thread.currentThread().getName}"))
      response <- requestF[IO]
      _        <- IO(println(s"Finished: ${Thread.currentThread().getName}"))
      _        <- IO(println(response))
    } yield ()

    app.guarantee(IO(ec.shutdown())) as ExitCode.Success
  }

  private def getGoogleStatus(): Either[Throwable, Int] = {
    Try(requests.get("https://www.google.com").statusCode).toEither
  }
}

/** `IO.never` represents a non-terminating `IO`
  */
object Never extends IOApp {
  def run(args: List[String]): IO[ExitCode] = IO.never
}

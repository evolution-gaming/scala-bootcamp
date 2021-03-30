package com.evolutiongaming.bootcamp.effects.v2

import cats._
import cats.effect._
import cats.syntax.all._

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.io.{Source, StdIn}
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

object ConsoleSimple {
  def readStr(): String = StdIn.readLine()
  def putStr(text: String): Unit = print(text)
}

object Simple extends App {
  import ConsoleSimple._

  putStr("What is your name: ")
  val name = readStr()
  putStr(s"Hello, $name!\n")
}



final case class User(id: Long, email: String)

object UserRepository {
  def upsert(email: String): Future[Long] = Future {
    println(s"Creating user $email")
    if (Random.nextBoolean()) 1L else sys.error("error")
  }
}

object EmailService {
  def send(user: User): Future[Unit] = Future(println(s"Notifying user ${user.email}"))
}

object FutureApp extends App {

  def create(email: String) = for {
    id   <- UserRepository.upsert(email)
    user =  User(id, email)
    _    <- EmailService.send(user)
  } yield user

  def cache(user: User): Future[Unit] = Future(println(s"Caching ${user.email}"))

  val app =
    create("test@evolution.com")
      .flatMap(user => cache(user))
}


/*
  IO monad represents computations as data, and this computations can be manipulated in various ways

  You may want to:
  – Map results
  - Compose two computations sequentially or in parallel
  - Handle errors
  – Manage resources
 */
object ConsoleIO {
  def readStr: IO[String] = IO(StdIn.readLine())
  def putStr(text: String): IO[Unit] = IO(print(text))
}

object IOHelloWorld extends App {
  import ConsoleIO._

  val app = for {
    _    <- putStr("What is your name: ")
    name <- readStr
    _    <- putStr(s"Hello, $name!")
  } yield ()

  app.unsafeRunSync()
}


trait Console[F[_]] {
  def readStr: F[String]
  def putStr(text: String): F[Unit]
}

// Exercise 1
object Console {
  def of[F[_]]: Console[F] = ???
}

object TFHelloWorld extends App {
  def app[F[_]: Monad](console: Console[F]): F[Unit] = for {
    _    <- console.putStr("What is your name: ")
    name <- console.readStr
    _    <- console.putStr(s"Hello, $name!")
  } yield ()

  app[IO](Console.of[IO]).unsafeRunSync()
}


/*
 * Handling errors - operations available for `MonadError` and `ApplicativeError` are available for `IO`.
 *
 * See:
 *  - https://typelevel.org/cats/api/cats/MonadError.html
 */
object ErrorsExample extends App {

  val readInt = ConsoleIO.readStr.map(_.toInt)

  readInt.foreverM.unsafeRunSync()
}



object ErrorsExercise {

  sealed trait ValidationError extends Throwable with NoStackTrace
  case object InvalidAge  extends ValidationError
  case object InvalidName extends ValidationError

  sealed abstract case class Age private (value: Int)
  object Age {
    def from(value: Int): Either[ValidationError, Age] =
      Either.cond(value > 0, new Age(value) {}, InvalidAge)
  }

  sealed abstract case class Name private(value: String)
  object Name {
    def from(value: String): Either[ValidationError, Name] =
      Either.cond(value.nonEmpty, new Name(value) {}, InvalidName)
  }

  final case class Person(name: Name, age: Age)

  // Exercise 2
  def readPerson[F[_]](console: Console[F]): F[Person] = {
    val readName: F[Name] = ???

    val readAge: F[Age] = ???

//    (readName, readAge).mapN(Person)
    ???
  }

  readPerson[IO](Console.of[IO]).unsafeRunSync()
}



object BracketApp extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    def sleepy(msg: String) = IO.sleep(1.second) *> IO(println(msg))

    def withCloseCase[A](io: IO[A]) = io.guaranteeCase {
      case ExitCase.Completed    => IO(println("Completed"))
      case ExitCase.Canceled     => IO(println("Canceled"))
      case ExitCase.Error(error) => IO(println(s"Error: ${error.getMessage}"))
    }

    val canceled = for {
      fiber <- withCloseCase(sleepy("Not gonna end")).start
      _     <- IO.sleep(500.millis)
      _     <- fiber.cancel
    } yield ()

    val bracketed =
      sleepy("Acquire")
        .bracket { _ =>
          IO(println("Use"))
        } { _ =>
          sleepy("Closed")
        }

    bracketed as ExitCode.Success
  }
}



object CatApp extends IOApp {

  private def cat(filename: String): Unit = {
    val acquire = Source.fromFile(filename)
    def use(source: Source): Unit = source.getLines().foreach(println)
    def close(source: Source): Unit = source.close()

    val source = acquire
    use(source)
    close(source)
  }

  // Exercise 3
  private def catTF[F[_]](console: Console[F], filename: String): F[Unit] = ???

  override def run(args: List[String]): IO[ExitCode] = {
    IO(StdIn.readLine()).map(cat) as ExitCode.Success
  }
}


/*
   * Asynchronous process - a process which continues its execution in a different place or time than the one
   * that started it.
   *
   * Concurrency - a program structuring technique in which there are multiple logical threads of control,
   * whose effects are interleaved.
   *
   * `IO.async` - describes an asynchronous process which cannot be cancelled
   */
object AsyncApp extends IOApp {

  private val ec = Executors.newFixedThreadPool(4)

  private def request(url: String, callback: Either[Throwable, Int] => Unit): Unit = {
    val thread = new Thread() {
      override def run(): Unit = {
        val status = Try(requests.get(url).statusCode)
        callback(status.toEither)
      }
    }

    ec.execute(thread)
  }

  def requestF[F[_]: Async](url: String): F[Int] = Async[F].async { cb =>
    println(Thread.currentThread().getName)
    val status = Try(requests.get(url).statusCode)
    cb(status.toEither)
  }

  override def run(args: List[String]): IO[ExitCode] = {
    val app = for {
      response <- IO(StdIn.readLine()) >>= requestF[IO]
      _        <- IO(println(response))
    } yield ()

    app.guarantee(IO(ec.shutdown())) as ExitCode.Success
  }
}

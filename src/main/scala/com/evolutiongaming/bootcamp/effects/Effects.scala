package com.evolutiongaming.bootcamp.effects

import java.util.concurrent.{Executors, ThreadFactory}
import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}

import cats.effect.{Blocker, ContextShift, ExitCode, IO, IOApp, Resource}

import scala.io.StdIn
import cats.implicits._

import scala.annotation.tailrec
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
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
 *  - Keeps referential transparency so it is easier to refactor our programs and reason about them
 *  - We can evaluate them when we want
 *  - They can be sequentially executed
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
 *  pattern), but we will not discuss this in this lecture.
 *
 * Asynchronous Effects, as opposed to Scala Future-s, are lazy. Nothing is run until an "unsafe" method
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
 *
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
 *
 * `IOApp` is the `App` equivalent for `IO`-based programs.
 */
object IOBuildingBlocks1 extends IOApp {
  private val nameProgram = for {
    _     <- putStrLn("What's your name?")
    name  <- readStrLn
    _     <- putStrLn(s"Hi, $name!")
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

  def process(console: Console, counter: Int = 0): IO[ExitCode] = ???

  override def run(args: List[String]): IO[ExitCode] = process(Console.Real)
}

object IOBuildingBlocks2 extends IOApp {
  /*
   * `IO.suspend` is equivalent to `IO(f).flatten` and can be used to avoid a stack overflow.
   *
   *   def suspend[A](thunk: => IO[A]): IO[A]
   *
   * `IO.flatMap` is also "trampolined" (that means - it is stack-safe).
   *
   * Question: What happens when `fib` is executed with a large enough `n`?
   * Question: How can we fix it using `IO.suspend`?
   */
  private def fib(n: Int, a: Long = 0, b: Long = 1): IO[Long] =
    n match {
      case 0 => IO.pure(a)
      case _ => fib(n - 1, b, a + b).map(_ + 0) // Question: Why did I add this useless `.map` here?
    }

  def run(args: List[String]): IO[ExitCode] =
    fib(100000)
      .flatMap(x => putStrLn(s"fib = $x")) as ExitCode.Success
}


object AsyncAndCancelable extends IOApp {
  import scala.concurrent.ExecutionContext.Implicits.global

  /*
   * Asynchronous process - a process which continues its execution in a different place or time than the one
   * that started it.
   *
   * Concurrency - a program structuring technique in which there are multiple logical threads of control,
   * whose effects are interleaved.
   *
   * `IO.async` - describes an asynchronous process which cannot be cancelled
   */
  private def tickNSeconds(n: Int): IO[Unit] =
    if (n <= 0) IO.unit
    else for {
      _ <- putStrLn(s"Tick $n")
      _ <- IO.sleep(1.second)
      _ <- tickNSeconds(n - 1)
    } yield ()

  private val asyncProgram = for {
    _ <- putStrLn("launching async")
    _ <- IO.async[Unit] { cb: (Either[Throwable, Unit] => Unit) =>
      val future = tickNSeconds(2).unsafeToFuture() // avoid 'unsafe*' in real code
      future onComplete { x: Try[Unit] =>
        cb(x.toEither)
      }
    }
    _ <- putStrLn("async finished")
  } yield ()

  /*
   * Cancellation is the ability to interrupt an IO task before the completion. You should make sure you
   * release any acquired resources.
   *
   * `IO.cancelable` - similar to `IO.async`, but should return an IO which captures the cancellation logic.
   *
   * `IO#start` - forks a new IO as a `Fiber` (you can think of them as lightweight threads). Fibers can be
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

  def run(args: List[String]): IO[ExitCode] = for {
    _ <- asyncProgram
    _ <- cancelableProgram1
    _ <- cancelableProgram2
  } yield ExitCode.Success
}

/* Cancellation on legacy code,
 * When writing your cancellable code, be aware that cancellation is a concurrent action. That is, there is no synchronization provided by Cats Effect IO for it.
 * Therefore, if your effect code is doing an operation that isn't safe to do concurrently with cancellation, it can lead data corruption or other errors.
 * You can solve it, for example, by introducing a lock, as per Cats Effect IO documentation here: ...
 * https://typelevel.org/cats-effect/datatypes/io.html#gotcha-cancellation-is-a-concurrent-action
 */
object CancelableResultsAndLegacy extends IOApp {

  private val cancelableLegacyIntegrationProgram = {
    class UglyLegacyCode {
      private val cancelled = new AtomicBoolean(false)

      private def longRecursiveCompute(x: Long, until: Long): Long = {
        if (cancelled.get()) {
          throw new InterruptedException("compute interrupted")
        } else if (x >= until) {
          x
        } else {
          println(s" ${Thread.currentThread().toString} Calculating in longRecursiveCompute: $x")
          Thread.sleep(1000)
          longRecursiveCompute(x + x, until)
        }
      }

      def compute(i: Long, until: Long)(onComplete: Long => Unit, onError: Exception => Unit): Unit = {
        val t = new Thread(() => {
          try {
            onComplete(longRecursiveCompute(i, until))
          } catch {
            case e: InterruptedException => onError(e)
          }
        })
        t.start()
      }

      def cancel(): Unit = cancelled.set(true)
    }

    for {
      _ <- putStrLn("Launching cancelable")
      io = IO.cancelable[Long] { cb =>
        val legacy = new UglyLegacyCode
        legacy.compute(2L, Long.MaxValue)(res => cb(Right(res)), e => cb(Left(e)))
        IO.delay(legacy.cancel())
      }
      fiber <- io.start
      _ <- putStrLn(s"Started $fiber")
      res <- IO.race(IO.sleep(10.seconds), fiber.join)
      _ <-
        res.fold(
          _ => putStrLn(s"cancelling $fiber...") *> fiber.cancel *> putStrLn("IO cancelled"),
          i => putStrLn(s"IO completed with: $i")
        )
    } yield ()
  }
  override def run(args: List[String]): IO[ExitCode] = for {
    _ <- cancelableLegacyIntegrationProgram
  } yield ExitCode.Success
}

/**
 * IO is cancellable on async boundary IO.shift or on IO.cancelBoundary
 * https://typelevel.org/cats-effect/datatypes/io.html#iocancelboundary
 */
object CancelBoundaries extends IOApp  {

  val nonCancelableProgram: IO[Unit] = {
    //program has no context shift and no cancel boundry set, it's not cancellable
    def nonCancellableTimes(rec: Int): IO[Unit] = for {
      _ <- putStrLn(s"Running remaining iterations: ${rec}")
      _ <- IO.sleep(1.seconds).uncancelable
      _ <- if(rec > 0) IO.suspend(nonCancellableTimes(rec - 1)) else IO.unit
    } yield ()

    for {
      _ <- putStrLn("Starting nonCancelableProgram")
          fib <- nonCancellableTimes(10).start
      _ <- IO.sleep(5.seconds)
      _ <- fib.cancel
      _ <- putStrLn("Cancelled nonCancelableProgram")
      _ <- IO.sleep(5.seconds) //just to keep program alive, otherwise deamon thread will be terminated
      _ <- putStrLn("End nonCancelableProgram")
    } yield ()
  }

  val cancelableProgram: IO[Unit] = {
    //on every iteration canel boundry is set, program is cancellable
    def cancellableTimes(rec: Int): IO[Unit] = for {
      _ <- putStrLn(s"Running remaining iterations: ${rec}")
      _ <- IO.sleep(1.seconds).uncancelable
      _ <- if(rec > 0) IO.cancelBoundary *> IO.suspend(cancellableTimes(rec - 1)) else IO.unit
    } yield ()

    for {
      _ <- putStrLn("Starting cancelableProgram")
      fib <- cancellableTimes(10).start
      _ <- IO.sleep(5.seconds)
      _ <- fib.cancel
      _ <- putStrLn("Cancelled cancelableProgram")
      _ <- IO.sleep(5.seconds) //just to keep program alive, otherwise deamon thread will be terminated
      _ <- putStrLn("End cancelableProgram")
    } yield ()
  }

  override def run(args: List[String]): IO[ExitCode] = for {
    _ <- cancelableProgram
    _ <- nonCancelableProgram
  } yield ExitCode.Success
}

/*
 * `sequence`     -   takes a list of `IO`, executes them in sequence and returns an `IO` with a collection
 *                    of all the results.
 *
 * `parSequence`  -   does the same, but executes in parallel
 */
object Sequence extends IOApp {
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

  def run(args: List[String]): IO[ExitCode] = for {
    _ <- sequenceProgram
    _ <- parSequenceProgram
  } yield ExitCode.Success
}

/*
 * `ContextShift` is the pure equivalent to `ExecutionContext`:
 * - https://typelevel.org/cats-effect/datatypes/contextshift.html
 *
 * `ContextSwitch#shift` or `IO.shift` can be used to do "cooperative yielding" by triggering a logical fork
 * so that the current thread is not occupied on long running operations.
 *
 * This forms an "async boundary".
 *
 * We can adjust `fib` to have async boundaries every 1000 invocations.
 */
object Shift extends IOApp {
  private val Default: ContextShift[IO] = implicitly[ContextShift[IO]]

  private def fibWithShift(n: Int, a: Long = 0, b: Long = 1): IO[Long] =  IO.suspend {
    n match {
      case 0 => IO.pure(a)
      case _ =>
        val next = fibWithShift(n - 1, b, a + b)
        if (n % 1000 == 0) Default.shift *> next
        else next
    }
  }

  private val cachedThreadPool = Executors.newCachedThreadPool()
  private val Blocking: ContextShift[IO] = IO.contextShift(ExecutionContext.fromExecutor(cachedThreadPool))

  private val shiftToSpecific: IO[Unit] = for {
    _     <- putStrLn("What's your name?")
    _     <- IO.shift(Blocking)
    name  <- readStrLn
    _     <- IO.shift(Default)
    _     <- putStrLn(s"Hi, $name!")
    _     <- IO(cachedThreadPool.shutdown())
  } yield ()

  def run(args: List[String]): IO[ExitCode] = for {
    _ <- fibWithShift(100000).flatMap(x => putStrLn(s"fibWithShift = $x"))
    _ <- shiftToSpecific
  } yield ExitCode.Success
}

/* Example showcases how does context shift works in terms of executing thread.
 * 1. cpu bound pool
 * 2. Blocker https://typelevel.org/cats-effect/datatypes/contextshift.html#blocker
 */
object Shift2 extends IOApp {

  def logLine(s: => String): IO[Unit] = IO.suspend(putStrLn(s"${Thread.currentThread().toString} $s"))

  def newThreadFactory(name: String): ThreadFactory = new ThreadFactory {
    val ctr = new AtomicInteger(0)
    def newThread(r: Runnable): Thread = {
      val back = new Thread(r, s"$name-pool-${ctr.getAndIncrement()}")
      back.setDaemon(true)
      back
    }
  }

  // dedicated pool with 2 threads for cpu bound tasks
  // io-app default pools size is calculted as math.max(2, Runtime.getRuntime().availableProcessors())
  // in case we want to restrict certain computation and not to interfere with global pool
  // abusing thread pools may lead to unnecessary context switches which will degrade performance
  val basicShiftingExample = {
    val cpuBoundPool: ExecutionContext =
      ExecutionContext
        .fromExecutor(Executors.newFixedThreadPool(2, newThreadFactory("cpu-bound")))

    val cpuBoundContext = IO.contextShift(cpuBoundPool)

    def cpuBound(d: Double, invocation: Long): IO[Double] = IO.suspend {
      if(d == 0.0) IO.pure(d)
      else cpuBound(d / 2.0, invocation + 1) //putStrLn(s"${Thread.currentThread().toString} current value: ${d}") *>
    }

    for {
      _ <- logLine(s"Started on default thread")
      _ <- ContextShift[IO].evalOn(cpuBoundPool)(logLine(s"Evaling on cpu-bound-pool"))
      _ <- logLine(s"We are back on main default")

      result <- cpuBoundContext.shift >> logLine(s"running on cpu-bound-pool-") *> cpuBound(100000.0, 0)
      _ <- IO.shift >> logLine(s"result=${result} result on default")
    } yield ()
  }

  // https://typelevel.org/cats-effect/datatypes/contextshift.html#blocker
  // special pool with explicit construct for blocking operations
  val blockingExample = {
    val blocker: Resource[IO, Blocker] = Blocker.fromExecutorService(IO.delay(Executors.newCachedThreadPool(newThreadFactory("blocker"))))

    blocker.use { blocker =>
      def blockingCall(id: Int): Unit = {
        println(s"${Thread.currentThread().toString} Starting blocking work id:$id")
        Thread.sleep(5000)
        println(s"${Thread.currentThread().toString} Ended work id:$id")
      }

      //launching paralell 10 blocking tasks
      (0 to 9).toList.map(id => blocker.delay[IO, Unit](blockingCall(id))).parSequence
    }
  }

  def run(args: List[String]): IO[ExitCode] = {
    for {
      _ <- basicShiftingExample
      _ <- blockingExample
      _ <- logLine("End")
    } yield ExitCode.Success
  }
}


/*
 * Handling errors - operations available for `MonadError` and `ApplicativeError` are available for `IO`.
 *
 * See:
 *  - https://typelevel.org/cats/api/cats/MonadError.html
 */
object HandlingErrors extends IOApp {
  private def failingProgram: IO[String] = for {
    // `raiseError` fails the `IO` with the specified exception
    _ <- IO.raiseError { new RuntimeException("error") }
  } yield "success"

  def run(args: List[String]): IO[ExitCode] = for {
    attempt           <-  failingProgram.attempt  // Either[Throwable, A]
    _                 <-  putStrLn(s"attempt = $attempt")

    option            <-  failingProgram.option   // Option[A]
    _                 <-  putStrLn(s"option = $option")

    handleError       <-  failingProgram.handleError(x => s"error:  ${x.getMessage}")
    _                 <-  putStrLn(s"handleError = $handleError")

    handleErrorWith   <-  failingProgram.handleErrorWith(x => IO.pure(s"error: ${x.getMessage}"))
    _                 <-  putStrLn(s"handleErrorWith = $handleErrorWith")

    recover           <-  failingProgram.recover {
                            case x if x.getMessage == "error" => s"error: ${x.getMessage}"
                          }
    _                 <-  putStrLn(s"recover = $recover")

    recoverWith       <-  failingProgram.recoverWith {
                            case x if x.getMessage == "error" => IO.pure(s"error: ${x.getMessage}")
                          }
    _                 <-  putStrLn(s"recoverWith = $recoverWith")

    redeem            <-  failingProgram.redeem(
                        (x: Throwable) => s"error: ${x.getMessage}",
                        (x: String) => s"success: $x",
                      )
    _                 <-  putStrLn(s"redeem = $redeem")

    redeemWith        <-  failingProgram.redeemWith(
                        (x: Throwable)  =>  IO.pure(s"error: ${x.getMessage}"),
                        (x: String)     =>  IO.pure(s"success: $x"),
                      )
    _                 <-  putStrLn(s"redeemWith = $redeemWith")
  } yield ExitCode.Success
}

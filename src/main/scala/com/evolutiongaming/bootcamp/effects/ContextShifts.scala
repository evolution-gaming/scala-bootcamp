package com.evolutiongaming.bootcamp.effects

import java.nio.file.{Files, Path, Paths}
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{Executors, ThreadFactory}

import cats.effect.{Blocker, ContextShift, ExitCode, IO, IOApp, Resource}
import cats.implicits._
import com.evolutiongaming.bootcamp.effects.Console.Real.putStrLn

import scala.concurrent.ExecutionContext

/*
 * `ContextShift` is the pure equivalent to `ExecutionContext`:
 * - https://typelevel.org/cats-effect/datatypes/contextshift.html
 *
 * `ContextSwitch#shift` or `IO.shift` can be used to do "cooperative yielding" by triggering a logical fork
 * so that the current thread is not occupied on long running operations.
 *
 * When choosing sizes for thread pools the rule of thumb is:
 * 1. cpu-bound tasks should have not more then available cores on the machine threads, should be used for pure
 *    computation only IO app default ContextShift constructed as `math.max(2, Runtime.getRuntime().availableProcessors())`
 * 2. blocking io tasks - unbounded thread pool used only for blocking tasks,
 *    avoid using for computation as that will lead to poor performance because of context switches
 * https://typelevel.org/cats-effect/concurrency/basics.html#thread-pools
 * https://typelevel.org/cats-effect/concurrency/basics.html#concurrency
 * https://monix.io/docs/3x/best-practices/blocking.html
 *
 * Failure to run blocking tasks on a separate pool (via blocker) will lead to thread pool starvation. See example.
 *
 * Example showcases how does context shift works in terms of executing thread.
 * 1. CPU bound pool
 * 2. Blocker https://typelevel.org/cats-effect/datatypes/contextshift.html#blocker
 */
object ContextShifts extends IOApp {

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
  // io-app default pools size is calculated as math.max(2, Runtime.getRuntime().availableProcessors())
  // in case we want to restrict certain computation and not interfere with global pool
  // abusing thread pools may lead to unnecessary context switches which will degrade performance
  val basicShiftingExample: IO[Unit] = {
    val cpuBoundPool: ExecutionContext =
      ExecutionContext
        .fromExecutor(Executors.newFixedThreadPool(2, newThreadFactory("cpu-bound")))

    val cpuBoundContext = IO.contextShift(cpuBoundPool)

    def cpuBound(d: Double, invocation: Long): IO[Double] = IO.suspend {
      if (d == 0.0) IO.pure(d)
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
  val blockingExample: IO[Unit] = {
    val blocker: Resource[IO, Blocker] = Blocker.fromExecutorService(IO.delay(Executors.newCachedThreadPool(newThreadFactory("blocker"))))

    blocker.use { blocker =>
      def blockingCall(id: Int): Unit = {
        println(s"${Thread.currentThread().toString} Starting blocking work id:$id")
        Thread.sleep(5000)
        println(s"${Thread.currentThread().toString} Ended work id:$id")
      }

      //launching parallel 10 blocking tasks
      (0 to 9).toList.map(id => blocker.delay[IO, Unit](blockingCall(id))).parSequence.void
    }
  }

  // Anti-pattern: running blocking tasks on cpu-bound pool
  val threadPoolStarvationExample: IO[Unit] = {
    val twoThreadPool: ExecutionContext =
      ExecutionContext
        .fromExecutor(Executors.newFixedThreadPool(2, newThreadFactory("cpu-bound")))
    val cs = IO.contextShift(twoThreadPool)

    def blockingCall(): Unit = {
      logLine("Starting blocking call").unsafeRunSync()
      Thread.sleep(5000)
      logLine("End blocking call").unsafeRunSync()
    }

    for {
      _ <- cs.shift *> logLine("Starting thread pool startvation example")
      _ <- logLine("Spawning blocking tasks on 2 thread pool")
      fib <- (0 to 9).toList.map(_ => cs.shift *> IO.delay(blockingCall())).parSequence.void.start
      _ <- cs.shift *> logLine("This will happen only when thread frees up, such use of cpu bound pool would cause entire program to freeze")
      _ <- fib.join
    } yield ()
  }

  def run(args: List[String]): IO[ExitCode] = {
    for {
      _ <- basicShiftingExample
      _ <- blockingExample
      _ <- threadPoolStarvationExample
      _ <- logLine("End")
    } yield ExitCode.Success
  }
}

object ContextShiftExerciseOne extends IOApp {

  /* Exercise #1
   * create program that does work on custom pool with 1 thread
   */
  val singleThreadProgram: IO[Unit] = IO.delay(???)

  /* Exercise #2
   * refactor program to do blocking work on blocker
   */
  val blockingProgram = {
    def listSourceFiles(root: Path) = IO.delay {
      Files
        .walk(root)
        .filter((t: Path) => t.toString.endsWith(".scala"))
        .toArray[Path]((i: Int) => new Array[Path](i)).toList
    }
    def linesOfCode(file: Path): IO[Long] =
      IO.delay(Files.lines(file).count())

    for {
      sourceFiles <- listSourceFiles(Paths.get("./src"))
      listOfLineLenghts <- sourceFiles.map(linesOfCode).parSequence
      linesOfCode = listOfLineLenghts.sum
      _ <- putStrLn(s"Total Lines of code: ${linesOfCode}")
    } yield ()
  }

  override def run(args: List[String]): IO[ExitCode] = for {
    _ <- singleThreadProgram
    _ <- blockingProgram
  } yield ExitCode.Success

}

package com.evolutiongaming.bootcamp.effects.v3

import cats.effect.{Blocker, ContextShift, ExitCode, IO, IOApp, Sync}
import cats.syntax.all._

import java.util.concurrent.{Executors, ThreadFactory}
import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import com.evolutiongaming.bootcamp.effects.ContextShifts.{logLine, newThreadFactory}

import java.nio.file.{Files, Path, Paths}
import scala.concurrent.duration._

/*
 * `ContextShift` is the pure equivalent to `ExecutionContext`:
 * - https://typelevel.org/cats-effect/docs/2.x/datatypes/contextshift
 *
 * `ContextSwitch#shift` or `IO.shift` can be used to do "cooperative yielding" by triggering a logical fork
 * so that the current thread is not occupied on long running operations.
 *
 * Some hints on how to use it:
 * - cpu-bound tasks, async non-blocking io, should have not more then available cores on the machine threads, should be used for pure
 *   computation only. IO app default ContextShift constructed as `math.max(2, Runtime.getRuntime().availableProcessors())`.
 * - cpu intensive workload that must be restricted to not to interfere with main application logic (picture/video conversion, crypto)
 * - blocking io tasks - unbounded thread pool used only for blocking tasks,
 *   avoid using for computation as that will lead to poor performance because of context switches
 * https://typelevel.org/cats-effect/docs/2.x/concurrency/basics#thread-pools
 * https://typelevel.org/cats-effect/docs/2.x/concurrency/basics#concurrency
 *
 * Disclaimer: Test and measure. When it comes to concurrency, nobody has an idea what you're doing.
 */
object ContextShifts extends IOApp {

  def logLine(s: => String): IO[Unit] = IO.suspend(IO.delay(println(s"${Thread.currentThread().toString} $s")))

  def newThreadFactory(name: String): ThreadFactory =
    new ThreadFactory {
      val ctr = new AtomicInteger(0)

      def newThread(r: Runnable): Thread = {
        val back = new Thread(r, s"$name-pool-${ctr.getAndIncrement()}")
        back.setDaemon(true)
        back
      }
    }

  def basicShiftingProgram: IO[Unit] = {

    val cpuExecutionContext: ExecutionContext = ExecutionContext.fromExecutor(
      Executors.newFixedThreadPool(2, newThreadFactory("cpu-bound"))
    )

    val cpuShift: ContextShift[IO] = IO.contextShift(cpuExecutionContext)

    def cpuBoundTask(i: Int): IO[Int] =
      if (i == 100_000_000) IO.pure(i)
      else cpuShift.shift *> (if (i % 10_000_000 == 0) logLine(s"Reached $i") else IO.unit) *> IO.suspend(cpuBoundTask(i + 1))

    for {
      _ <- logLine("Started")
      _ <- cpuShift.shift // <- different thread pool
      _ <- cpuBoundTask(1)
      _ <- ContextShift[IO].shift // == IO.shift
//        _ <- ContextShift[IO].evalOn(cpuExecutionContext)(cpuBoundTask(1))
      _ <- logLine("Finished")
    } yield ()
  }

  /**
    * https://typelevel.org/cats-effect/docs/2.x/datatypes/contextshift#blocker
    *
    * Special pool with explicit construct for blocking operations.
    * Widely used together with blocking API's/Java API's, blocking db drivers etc.
    * Usually backed by cachedTreadPool
    */
  def blockingProgram: IO[Unit] = {

    val blocker = Blocker.fromExecutorService(
      IO.delay(Executors.newCachedThreadPool(newThreadFactory("blocker-pool")))
    )

    def blockingCall(id: Int): Unit = {
      println(s"${Thread.currentThread().toString} Starting blocking work id:$id")
      Thread.sleep(5000)
      println(s"${Thread.currentThread().toString} Finished blocking work id:$id")
    }

    blocker.use { blocker =>
            (0 to 100).toList.map(i => blocker.blockOn(IO.delay(blockingCall(i)))).parSequence.void
//      (0 to 100).toList.parTraverse(i => blocker.blockOn(IO.delay(blockingCall(i)))).void
//      (0 to 100).toList.parTraverse(i => ContextShift[IO].blockOn(blocker)(IO.delay(blockingCall(i)))).void
    }
  }

  def threadStarvationProgram: IO[Unit] = {

    val cpuExecutionContext: ExecutionContext = ExecutionContext.fromExecutor(
      Executors.newFixedThreadPool(2, newThreadFactory("cpu-bound"))
    )

    val cpuShift: ContextShift[IO] = IO.contextShift(cpuExecutionContext)

    def blockingCall(id: Int): Unit = {
      println(s"${Thread.currentThread().toString} Starting blocking work id:$id")
      Thread.sleep(5000)
      println(s"${Thread.currentThread().toString} Finished blocking work id:$id")
    }

    for {
      _ <- logLine("Started")
      fiber <- (0 to 9).toList.parTraverse(i => cpuShift.shift *> IO.delay(blockingCall(i))).void.start
      _ <- cpuShift.shift *> logLine("Finished")
      _ <- fiber.join
    } yield ()
  }

  def run(args: List[String]): IO[ExitCode] =
//    basicShiftingProgram.as(ExitCode.Success)
//    blockingProgram.as(ExitCode.Success)
    threadStarvationProgram.as(ExitCode.Success)
}

object ContextShiftsExercise extends IOApp {

  /* Exercise #1
   * Print "hello" 20 times with 1 second interval. Use single threaded pool for this.
   */
  def singleThreadProgram: IO[Unit] = ???

  /* Exercise #2
   * Refactor program to do blocking work using Blocker
   */
  def blockingProgram: IO[Unit] = {

    def listSourceFiles(root: Path): IO[List[Path]] =
      IO.delay {
        Files
          .walk(root)
          .filter((t: Path) => t.toString.endsWith(".scala"))
          .toArray[Path]((i: Int) => new Array[Path](i))
          .toList
      }

    def linesOfCode(file: Path): IO[Long] =
      IO.delay(Files.lines(file).count())

    for {
      sourceFiles      <- listSourceFiles(Paths.get("./src"))
      listOfLineLength <- sourceFiles.map(linesOfCode).parSequence
      linesOfCode       = listOfLineLength.sum
      _                <- IO.delay(println((s"Total Lines of code: $linesOfCode")))
    } yield ()
  }

  def run(args: List[String]): IO[ExitCode] =
    for {
      _ <- singleThreadProgram
//      _ <- blockingProgram
    } yield ExitCode.Success
}

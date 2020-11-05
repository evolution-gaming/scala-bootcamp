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
 * 2. cpu intensive workload that must be restricted to not to interfere with main application logic (picture/video conversion, crypto)
 * 3. blocking io tasks - unbounded thread pool used only for blocking tasks,
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

  // Lets create basic exercise:
  // * contextShift that is backed by 2 thread pool
  // * program that runs on this thread pool
  // * observe thread name, to see how task are scheduled on jvm threads
  val basicShiftingExample: IO[Unit] = {
    def cpuBound(i: Int): IO[Int] =
      if (i == Int.MaxValue) IO.pure(i)
      else IO.suspend(cpuBound(i+1)) //putStrLn(s"${Thread.currentThread().toString} current value: ${d}") *>


    IO.delay(???)
  }

  // https://typelevel.org/cats-effect/datatypes/contextshift.html#blocker
  // special pool with explicit construct for blocking operations
  // widely used together with blocking API's/Java API's, blocking db drivers...
  // usually backed by cachedTreadPool
  val blockingExample: IO[Unit] = {
    def blockingCall(id: Int): Unit = {
      println(s"${Thread.currentThread().toString} Starting blocking work id:$id")
      Thread.sleep(5000)
      println(s"${Thread.currentThread().toString} Ended work id:$id")
    }

    IO.delay(???)
  }

  // Anti-pattern: running blocking tasks on cpu-bound pool
  // Do not ever block on default contextShift or other specialized cpu-bound contextShift
  // as that will make your program unresponsive
  val threadPoolStarvationExample: IO[Unit] = {
    def blockingCall(id: Int): Unit = {
      println(s"${Thread.currentThread().toString} Starting blocking work id:$id")
      Thread.sleep(5000)
      println(s"${Thread.currentThread().toString} Ended work id:$id")
    }

    IO.delay(???)
  }

  def run(args: List[String]): IO[ExitCode] = {
    for {
      _ <- basicShiftingExample
//      _ <- blockingExample
//      _ <- threadPoolStarvationExample
      _ <- logLine("End")
    } yield ExitCode.Success
  }
}

object ContextShiftExerciseOne extends IOApp {

  /* Exercise #1
   * create ContextShift that is backed by single thread thread pool
   * and a program that does work this ContextShift, printing "hello" every second 100 times and completes the program
   */
  val singleThreadProgram: IO[Unit] = {

    IO.delay(???)
  }

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

package com.evolutiongaming.bootcamp.effects

import java.nio.file.{Files, Path, Paths}
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{Executors, ThreadFactory}

import cats.effect.{Blocker, ContextShift, ExitCode, IO, IOApp, Resource}
import cats.implicits._
import com.evolutiongaming.bootcamp.effects.Console.Real.putStrLn
import com.evolutiongaming.bootcamp.effects.ContextShifts.{logLine, newThreadFactory}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

/*
 * `ContextShift` is the pure equivalent to `ExecutionContext`:
 * - https://typelevel.org/cats-effect/datatypes/contextshift.html
 *
 * `ContextSwitch#shift` or `IO.shift` can be used to do "cooperative yielding" by triggering a logical fork
 * so that the current thread is not occupied on long running operations.
 *
 * When choosing sizes for thread pools the rule of thumb is:
 * 1. cpu-bound tasks, async non-blocking io, should have not more then available cores on the machine threads, should be used for pure
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
  // * observe how un-necessary context shifts could impact performance
  // * how shifting impacts task (fiber in cats-effects) cooperation
  // * how it compares to scala Future
  val basicShiftingExample: IO[Unit] = {

    val cpuExecutionCtx = ExecutionContext
      .fromExecutor(Executors.newFixedThreadPool(2, newThreadFactory("cpu-bound")))
    val cpuShift = IO.contextShift(cpuExecutionCtx)

    def cpuBound(i: Int): IO[Int] =
      if (i == Int.MaxValue) IO.pure(i)
      else IO.shift *> (if(i % 1000000 == 0) logLine(s"Reached $i") else IO.unit) *> IO.suspend(cpuBound(i+1))

    for {
      _ <- logLine("Staring on default cs")
      _ <- ContextShift[IO].evalOn(cpuExecutionCtx)(logLine("starting io") *> cpuBound(1)  *> logLine("done"))
//      _ <- cpuShift.shift *> logLine("starting io") *> cpuBound(1)  *> logLine("done") *> ContextShift[IO].shift
      _ <- logLine("end")
    } yield()
  }

  // https://typelevel.org/cats-effect/datatypes/contextshift.html#blocker
  // special pool with explicit construct for blocking operations
  // widely used together with blocking API's/Java API's, blocking db drivers...
  // usually backed by cachedTreadPool
  val blockingExample: IO[Unit] = {
    val blocker = Blocker.fromExecutorService(IO.delay(Executors.newCachedThreadPool(newThreadFactory("blocker-pool"))))

    def blockingCall(id: Int): Unit = {
      println(s"${Thread.currentThread().toString} Starting blocking work id:$id")
      Thread.sleep(5000)
      println(s"${Thread.currentThread().toString} Ended work id:$id")
    }

    blocker.use { blocker =>
      (0 to 100).toList.map(i => blocker.blockOn[IO, Unit](IO.delay(blockingCall(i)))).parSequence.void
    }
  }

  // Anti-pattern: running blocking tasks on cpu-bound pool
  // Do not ever block on default contextShift or other specialized cpu-bound contextShift
  // as that will make your program unresponsive
  val threadPoolStarvationExample: IO[Unit] = {
    val cpuExecutionCtx = ExecutionContext
      .fromExecutor(Executors.newFixedThreadPool(2, newThreadFactory("cpu-bound")))
    val cpuShift = IO.contextShift(cpuExecutionCtx)

    def blockingCall(id: Int): Unit = {
      println(s"${Thread.currentThread().toString} Starting blocking work id:$id")
      Thread.sleep(5000)
      println(s"${Thread.currentThread().toString} Ended work id:$id")
    }

    for {
      _ <- logLine("Starting blocked cs")
      fib <- (0 to 9).toList.map(i => cpuShift.shift *> IO.delay(blockingCall(i))).parSequence.void.start
      _ <- logLine("XXXXX Starting blocked cs")
      _ <- cpuShift.shift
      _ <- logLine("HYYY starting blocked cs")
      _ <- logLine("!!! End !!!")
      _ <- fib.join
    } yield()
  }

  def run(args: List[String]): IO[ExitCode] = {
    for {
//      _ <- basicShiftingExample
//      _ <- blockingExample
      _ <- threadPoolStarvationExample
//      _ <- logLine("End")s
    } yield ExitCode.Success
  }
}

object ContextShiftExerciseOne extends IOApp {

  /* Exercise #1
   * create ContextShift that is backed by single thread thread pool
   * and a program that does work this ContextShift, printing "hello" every second,
   * 100 times and completes the program
   */
  val singleThreadProgram: IO[Unit] = {
    val cpuExecutionCtx = ExecutionContext
      .fromExecutor(Executors.newFixedThreadPool(1, newThreadFactory("my-single-pool")))
    val cpuShift = IO.contextShift(cpuExecutionCtx)
    (0 to 99).toList.map(_ => cpuShift.shift *> logLine("hello") *> IO.sleep(1.second)).sequence.void
  }

  /* Exercise #2
   * refactor program to do blocking work on blocker
   */
  val blockingProgram = {
    val blocker = Blocker.fromExecutorService(
      IO.delay(Executors.newCachedThreadPool(newThreadFactory("blocker-pool")))
    )

    def listSourceFiles(root: Path) = IO.delay {
      Files
        .walk(root)
        .filter((t: Path) => t.toString.endsWith(".scala"))
        .toArray[Path]((i: Int) => new Array[Path](i)).toList
    }
    def linesOfCode(file: Path): IO[Long] =
      IO.delay(Files.lines(file).count())

    blocker.use { blocker =>
      for {
        sourceFiles <- blocker.blockOn(listSourceFiles(Paths.get("./src")))
        listOfLineLenghts <- blocker.blockOn(sourceFiles.map(linesOfCode).parSequence)
        linesOfCode = listOfLineLenghts.sum
        _ <- putStrLn(s"Total Lines of code: ${linesOfCode}")
      } yield ()
    }
  }

  override def run(args: List[String]): IO[ExitCode] = for {
//    _ <- singleThreadProgram
    _ <- blockingProgram
  } yield ExitCode.Success

}

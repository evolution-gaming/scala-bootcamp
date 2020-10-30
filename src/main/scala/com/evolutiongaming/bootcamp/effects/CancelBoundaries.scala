package com.evolutiongaming.bootcamp.effects

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits.catsSyntaxParallelSequence
import com.evolutiongaming.bootcamp.effects.Console.Real.putStrLn

import scala.concurrent.duration._
import scala.util.Random
import scala.util.control.NonFatal

/*
 * IO is cancellable only on async boundary `IO.shift` or on `IO.cancelBoundary` and after 512 flatMap loop iterations.
 * Documentation states:
 *
 *   We should also note that flatMap chains are only cancelable only if the chain happens after an asynchronous
 *   boundary.
 *
 *   After an asynchronous boundary, cancellation checks are performed on every N flatMap. The value of N is hardcoded to
 *   512.
 *
 * This is bit misleading, because cancellation is checked and counter IS reset on async boundary,
 * but the counter is still taken into account even if not crossing async boundaries.
 *
 * Technically IO is switching from Main to io-app context.
 *
 * That may lead to inconsistent state when doing `race` with internal state update.
 *
 * That means - critical blocks should be marked as `uncancellable`
 *
 * https://typelevel.org/cats-effect/datatypes/io.html#concurrency-and-cancellation
 * https://typelevel.org/cats-effect/datatypes/io.html#iocancelboundary
 */
object CancelBoundaries extends IOApp  {

  val nonCancelableProgram: IO[Unit] = {
    //program has no context shift and no cancel boundary set, it's not cancellable
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
    //on every iteration cancel boundary is set, program is cancellable
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

object CancelBoundariesExercises extends IOApp {
  def delay(duration: FiniteDuration): IO[Unit] = IO.sleep(duration).uncancelable

  /* Exercise #1
   * Fix retry function without altering delay function, to be cancellable immediately, so that running the program
   * there is no retrying after cancel
   */
  val retryExercise: IO[Unit] = {
    implicit class ioRetrySyntax[A](task: IO[A]) {
      def retry(id: String, maxRetries: Int, interval: FiniteDuration): IO[A] =
        task
          .handleErrorWith {
            case NonFatal(e) =>
              putStrLn(s"$id Retrying... retries left: $maxRetries") *> (if(maxRetries <= 0) IO.raiseError(e)
              else delay(interval) *> IO.suspend(task.retry(id, maxRetries-1, interval)))
          }
    }

    val io = IO.delay(if(Random.nextBoolean()) throw new RuntimeException("kaboom!") else "SUCCESS!")
    for {
      fib <- (0 to 10).toList.map(id => io.retry(s"id:$id", 10, 5.second))
        .parSequence.flatMap(ll => putStrLn(ll.toString())).start
      _ <- IO.sleep(5.seconds)
      _ <- fib.cancel
      _ <- putStrLn("No more work after this point")
      _ <- IO.sleep(30.seconds)
      _ <- putStrLn(s"End")
    } yield ()
  }

  /* Exercise #2
   * Fix program so that no Calculation is happening after cancellation
   */
  val computeExercise = {
    def cpuBoundCompute(value: BigInt, multiplier: BigInt): IO[BigInt] = {
      val log = IO.delay(println(s"${Thread.currentThread().toString} Calculating... ${multiplier}"))
      log *> IO.suspend(cpuBoundCompute(value * multiplier, multiplier + 1))
    }
    for {
      _ <- putStrLn("Starting program")
      fib <- cpuBoundCompute(1, 1).start
      _ <- fib.cancel
      _ <- putStrLn("cpu bound cancelled")
      _ <- IO.sleep(10.seconds)
    } yield ()
  }

  /* Exercise #3
   * Try https://typelevel.org/cats-effect/datatypes/io.html#concurrency-and-cancellation
   * first notCancelable example, try starting it as fiber, then cancelling. What is the behaviour?
   */
  override def run(args: List[String]): IO[ExitCode] = for {
    _ <- retryExercise
    _ <- computeExercise
  } yield ExitCode.Success
}

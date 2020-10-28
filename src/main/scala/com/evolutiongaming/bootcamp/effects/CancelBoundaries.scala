package com.evolutiongaming.bootcamp.effects

import cats.effect.{ExitCode, IO, IOApp}
import com.evolutiongaming.bootcamp.effects.Console.Real.putStrLn

import scala.concurrent.duration._

/*
 * IO is cancellable only on async boundary IO.shift or on IO.cancelBoundary
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
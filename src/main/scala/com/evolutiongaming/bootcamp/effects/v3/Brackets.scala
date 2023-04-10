package com.evolutiongaming.bootcamp.effects.v3

import cats.effect.kernel.Outcome
import cats.effect.{ExitCode, IO, IOApp}

import scala.concurrent.duration._
import scala.io.{BufferedSource, Source}

object GuaranteeApp extends IOApp {

  def sleepy(msg: String): IO[Unit] = IO.sleep(1.second) *> IO(println(msg))

  def withGuaranteeCase[A](io: IO[A]): IO[A] =
    io.guaranteeCase {
      case Outcome.Succeeded(fa) => IO.delay(println("Completed"))
      case Outcome.Canceled()    => IO.delay(println("Canceled"))
      case Outcome.Errored(e)    => IO.delay(println(s"Error: ${e.getMessage}"))
    }

  def cancelledProgram: IO[Unit] =
    for {
      fiber <- withGuaranteeCase(sleepy("Not gonna finish")).start
      _     <- IO.sleep(500.millis)
      _     <- fiber.cancel
    } yield ()

  def run(args: List[String]): IO[ExitCode] =
    cancelledProgram.as(ExitCode.Success)
}

object BracketApp extends IOApp {

  def acquire(name: String): IO[BufferedSource] = IO(Source.fromFile(name))
  def release(source: BufferedSource): IO[Unit] = IO(source.close())

  def readSource(source: BufferedSource): IO[Iterator[String]] = IO(source.getLines())

  def bracketProgram: IO[Unit] =
    acquire("ReadMe.md")
      .bracket { bufferedSource =>
        readSource(bufferedSource)
          .map(_.mkString("\n"))
          .flatMap(str => IO.delay(println(str)))
      } { bufferedSource =>
        release(bufferedSource) // <- this operation is concurrent with `use` section in case of cancellation
      }

  def run(args: List[String]): IO[ExitCode] =
    bracketProgram.as(ExitCode.Success)
}

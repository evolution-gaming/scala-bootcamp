package com.evolution.bootcamp.inspect

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp

object Marshak extends IOApp {

  implicit val console = Console.create[IO]
  val printer = Printer.create[IO]

  def run(args: List[String]): IO[ExitCode] = {
    printer.print as ExitCode.Success
  }

}

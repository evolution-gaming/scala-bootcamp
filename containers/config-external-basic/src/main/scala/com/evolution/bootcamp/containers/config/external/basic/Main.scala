package com.evolution.bootcamp.containers.config.external.basic

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits.toBifunctorOps
import com.evolution.bootcamp.containers.config.shared.BasicConf
import pureconfig.ConfigSource
import pureconfig.generic.auto._

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    (for {
      _ <- IO(println("Basic external config example"))
      loaded <- IO(ConfigSource.file("./conf/global.conf").load[BasicConf])
      config <- IO.fromEither(loaded.leftMap(x => new RuntimeException(x.prettyPrint())))
      _ <- IO(println(config))
    } yield ()).as(ExitCode.Success)
  }

}

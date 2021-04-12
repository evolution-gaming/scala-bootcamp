package com.evolutiongaming.bootcamp.tf.shopping

import cats.MonadError
import cats.data.{Kleisli, OptionT}
import cats.syntax.all._
import com.evolutiongaming.bootcamp.tf.shopping.effects.Console

trait ConsoleInterface[F[_]] {
  def repl: F[Unit]
}

object ConsoleInterface {

  def apply[F[_]: Console: MonadError[*[_], Throwable]](
    router: Kleisli[OptionT[F, *], List[String], String]
  ): ConsoleInterface[F] = new ConsoleInterface[F] {
    override def repl: F[Unit] = {
      val result = for {
        line        <- Console[F].readStrLn
        args         = line.split(" ").toList
        result      <- router(args).value
        resultString = result.getOrElse("Can't find router for the requested command")
        _           <- Console[F].putStrLn(resultString)
      } yield ()

      result.handleErrorWith { error =>
        Console[F].putStrLn(s"Unexpected error: ${error.getMessage}")
      } >> repl
    }
  }

}

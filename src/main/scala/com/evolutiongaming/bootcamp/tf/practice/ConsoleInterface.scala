package com.evolutiongaming.bootcamp.tf.practice

import cats.MonadThrow
import cats.data.{Kleisli, OptionT}
import cats.syntax.all._
import com.evolutiongaming.bootcamp.tf.practice.effects.Console

trait ConsoleInterface[F[_]] {
  def repl: F[Unit]
}

object ConsoleInterface {

  def apply[F[_]: MonadThrow: Console](
    router: Kleisli[OptionT[F, *], List[String], String]
  ): ConsoleInterface[F] =
    new ConsoleInterface[F] {
      def repl: F[Unit] = {
        val loop = for {
          line        <- Console[F].readLine // cart add ...
          args         = line.split(" ").toList
          result      <- router(args).value
          resultString = result.getOrElse("Can't find router for the requested command")
          _           <- Console[F].putLine(resultString)
        } yield ()

        loop.handleErrorWith { error =>
          Console[F].putLine(s"Unexpected error: ${error.getMessage}")
        } >> repl // >> is the same as *> but suits for recursion, i.e. stack safe
      }
    }
}

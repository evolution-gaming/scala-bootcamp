package com.evolutiongaming.bootcamp.tf.practice

import cats.MonadThrow
import cats.syntax.all._
import cats.data.{Kleisli, OptionT}
import com.evolutiongaming.bootcamp.tf.practice.effects.Console

trait ConsoleInterface[F[_]] {
  def repl: F[Unit]
}

object ConsoleInterface {

  def apply[F[_]: MonadThrow](
    console: Console[F],
    router: Kleisli[OptionT[F, *], List[String], String]
  ): ConsoleInterface[F] =
    new ConsoleInterface[F] {
      def repl: F[Unit] = {
        val loop: F[Unit] = for {
          line        <- console.readString
          args         = line.split(" ").toList
          result      <- {
            val a: OptionT[F, String] = router(args)
            a.value
          }
          resultString = result.getOrElse("Can't find router for the requested command")
          _           <- console.putString(resultString)
        } yield ()

        loop.handleErrorWith { error =>
          console.putString(s"Unexpected error: ${error.getMessage}")
        } >> repl // >> is the same as *>, but it suits for recursion
      }
    }
}

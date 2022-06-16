package com.evolutiongaming.bootcamp.tf.practice.effects

import cats.effect.Async
import cats.syntax.all._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

trait FromFuture[F[_]] {
  def apply[A](future: => Future[A]): F[A]
}

object FromFuture {

  def apply[F[_]: FromFuture]: FromFuture[F] = implicitly

  def lift[F[_]: Async](implicit executionContext: ExecutionContext): FromFuture[F] =
    new FromFuture[F] {

      def apply[A](future: => Future[A]): F[A] =
        for {
          future <- Async[F].delay(future)
          result <- future.value.fold {
                      Async[F].async[A] { callback =>
                        future.onComplete { a =>
                          callback(a.toEither)
                        }
                      }
                    } {
                      {
                        case Success(a) => Async[F].pure(a)
                        case Failure(e) => Async[F].raiseError(e)
                      }
                    }
        } yield result
    }
}

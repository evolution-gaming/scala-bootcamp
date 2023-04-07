package com.evolutiongaming.bootcamp.iotf_practice

import cats.Monad
import cats.syntax.all._

object ex4 {
  case class Message(value: String)

  trait Send[F[_]] {

    /** Send the message to all specified players by their ids
      * @return
      *   may return connection errors
      */
    def send(ids: Set[Long], msg: Message): F[Unit]
  }

  trait AllIdsCache[F[_]] {

    /** @return
      *   always returns set of ids of players
      */
    def get: F[Set[Long]]
  }

  /*
   * We also have additional traits to be able handle and raise specific errors
   */
  trait Raise[F[_], E] {
    def raise(error: E): F[Unit]
  }
  trait Handle[F[_], E] {
    def handle[A](fa: F[A]): F[Either[E, A]]
  }

  /*
   * We have special Error class which will be handled at the end of our service
   */
  case class ApiError(status: Int, message: String) extends Throwable

  /** Sends messages to players. Handles all connection errors, logs them, and can return only ApiErrors
    */
  trait SendTo[F[_]] {
    def toEveryone(msg: Message): F[Unit]
    def toPlayer(id: Long, msg: Message): F[Unit]
  }
  object SendTo {
    /*
     * Task: improve SendTo implementation to complete conditions from the comment
     */
    def make[F[_]: Monad](
      cache: AllIdsCache[F],
      send: Send[F],
    )(implicit handle: Handle[F, Throwable], raise: Raise[F, ApiError]): SendTo[F] =
      new SendTo[F] {
        def toEveryone(msg: Message): F[Unit] =
          cache.get.flatMap { ids =>
            send.send(ids, msg)
          }

        def toPlayer(id: Long, msg: Message): F[Unit] =
          send.send(Set(id), msg)
      }
  }

  /*
   * Homework: write a test for `SendTo` service using `cats.IO` and `Either[Throwable, *]`
   * You will have to implement `Handle` and `Raise` for them and test that `SendTo` uses them correctly
   */
}

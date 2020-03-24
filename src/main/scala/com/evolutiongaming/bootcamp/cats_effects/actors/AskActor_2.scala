package com.evolutiongaming.bootcamp.cats_effects.actors

import cats.effect.concurrent.{Deferred, Semaphore}
import cats.effect.{ContextShift, IO}
import cats.implicits._

trait AskActor_2[In] {
  protected implicit def contextShift: ContextShift[IO]
  protected def semaphore: Semaphore[IO]

  def handleMessage: (In, Option[TempActor[In]]) => IO[Unit]

  def ask(entity: In): IO[In] =
    for {
      deferred_ <- Deferred[IO, In]
      _ <- semaphore.withPermit(
        handleMessage(
          entity,
          new TempActor[In] {
            override def deferred: Deferred[IO, In] = deferred_
          }.some)
      ).start
      result <- deferred_.get
    } yield result


  def sendMessage(
    entity: In
  ): IO[Unit] =
    semaphore.withPermit(
      handleMessage(entity, None)
    ).start *> IO.unit

  trait TempActor[In] {
    def deferred: Deferred[IO, In]
    def sendMessage(
      entity: In
    ): IO[Unit] =
      deferred.complete(entity)
  }
}

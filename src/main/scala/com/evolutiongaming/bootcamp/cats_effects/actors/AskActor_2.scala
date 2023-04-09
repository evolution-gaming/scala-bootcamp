package com.evolutiongaming.bootcamp.cats_effects.actors

import cats.effect.IO
import cats.effect.kernel.Deferred
import cats.effect.std.Semaphore
import cats.implicits._

trait AskActor_2[In] {
  protected def semaphore: Semaphore[IO]

  def handleMessage: (In, Option[TempActor]) => IO[Unit]

  def ask(entity: In): IO[In] =
    for {
      deferred_ <- Deferred[IO, In]
      _         <- semaphore.permit
        .surround(
          handleMessage(
            entity,
            new TempActor {
              override def deferred: Deferred[IO, In] = deferred_
            }.some,
          )
        )
        .start
      result    <- deferred_.get
    } yield result

  def sendMessage(
    entity: In
  ): IO[Unit] =
    semaphore.permit
      .surround(
        handleMessage(entity, None)
      )
      .start *> IO.unit

  trait TempActor {
    def deferred: Deferred[IO, In]
    def sendMessage(
      entity: In
    ): IO[Unit] =
      deferred.complete(entity).void
  }
}

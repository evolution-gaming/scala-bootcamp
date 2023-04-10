package com.evolutiongaming.bootcamp.cats_effects.actors

import cats.effect.IO
import cats.effect.std.Semaphore

trait Actor_3[In] {
  protected def semaphore: Semaphore[IO]
  protected def name: String
  protected def handleMessage: In => IO[Unit]

  def sendMessage(
    entity: In
  ): IO[Unit] =
    semaphore.permit
      .surround(
        handleMessage(entity)
      )
      .start *> IO.unit
}

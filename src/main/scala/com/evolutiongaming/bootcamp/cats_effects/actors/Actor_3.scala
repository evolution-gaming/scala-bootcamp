package com.evolutiongaming.bootcamp.cats_effects.actors

import cats.effect.concurrent.Semaphore
import cats.effect.{ContextShift, IO}
import cats.implicits._

trait Actor_3[In] {
  protected implicit def contextShift: ContextShift[IO]
  protected def semaphore: Semaphore[IO]
  protected def name: String
  protected def handleMessage: In => IO[Unit]

  def sendMessage(
    entity: In
  ): IO[Unit] =
    semaphore.withPermit(
      handleMessage(entity)
    ).start *> IO.unit
}

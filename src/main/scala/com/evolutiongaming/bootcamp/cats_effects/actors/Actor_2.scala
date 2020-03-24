package com.evolutiongaming.bootcamp.cats_effects.actors

import cats.effect.concurrent.Semaphore
import cats.effect.{ContextShift, IO}
import com.evolutiongaming.bootcamp.cats_effects.actors.model.Context
import cats.implicits._

trait Actor_2[In] {
  protected implicit def contextShift: ContextShift[IO]
  protected def semaphore: Semaphore[IO]
  protected def name: String
  protected def handleMessage[SenderIn]: (
    IO[In],
      IO[Context[SenderIn]],
      IO[In => SenderIn],
      IO[SenderIn => In]
    ) => IO[Unit]

  def sendMessage[SenderIn](
    entity: IO[In],
    context: IO[Context[SenderIn]],
    f: IO[In => SenderIn],
    f1: IO[SenderIn => In]
  ): IO[Unit] =
    semaphore.withPermit(
      handleMessage(
        entity,
        context,
        f,
        f1
      )
    ).start *> IO.unit
}

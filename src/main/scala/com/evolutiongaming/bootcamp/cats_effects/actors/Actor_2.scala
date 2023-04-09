package com.evolutiongaming.bootcamp.cats_effects.actors

import cats.effect.IO
import cats.effect.std.Semaphore
import com.evolutiongaming.bootcamp.cats_effects.actors.model.Context

trait Actor_2[In] {
  protected def semaphore: Semaphore[IO]
  protected def name: String
  protected def handleMessage[SenderIn]: (
    IO[In],
    IO[Context[SenderIn]],
    IO[In => SenderIn],
    IO[SenderIn => In],
  ) => IO[Unit]

  def sendMessage[SenderIn](
    entity: IO[In],
    context: IO[Context[SenderIn]],
    f: IO[In => SenderIn],
    f1: IO[SenderIn => In],
  ): IO[Unit] =
    semaphore.permit
      .surround(
        handleMessage(
          entity,
          context,
          f,
          f1,
        )
      )
      .start *> IO.unit
}

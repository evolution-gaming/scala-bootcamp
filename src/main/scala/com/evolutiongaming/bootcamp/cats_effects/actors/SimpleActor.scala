package com.evolutiongaming.bootcamp.cats_effects.actors

import cats.effect.IO
import cats.effect.kernel.Temporal
import cats.effect.std.Semaphore
import com.evolutiongaming.bootcamp.cats_effects.actors.model.Message

class SimpleActor(
  val semaphore: Semaphore[IO]
)(implicit
  timer: Temporal[IO]
) extends Actor_3[Message] {
  override protected def name: String                       = "SimpleActor"
  override protected def handleMessage: Message => IO[Unit] = { case Message(v, replyTo) =>
    replyTo.sendMessage(v.length)
  }
}

object SimpleActor {
  def apply(implicit timer: Temporal[IO]): IO[SimpleActor] =
    Semaphore[IO](1).map(new SimpleActor(_)(timer))
}

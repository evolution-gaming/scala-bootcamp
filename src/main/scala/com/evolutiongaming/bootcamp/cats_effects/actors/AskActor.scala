package com.evolutiongaming.bootcamp.cats_effects.actors

import cats.effect.IO

trait AskActor[In, Out] extends Actor[In] {
  protected def handleMessageWithResult: In => IO[Out]

  def ask(
    entity: In
  ): IO[Out] =
    semaphore.withPermit(
      handleMessageWithResult(entity)
    )
}

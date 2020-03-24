package com.evolutiongaming.bootcamp.cats_effects.actors

import cats.effect.concurrent.Semaphore
import cats.effect.{ContextShift, IO, Timer}
import com.evolutiongaming.bootcamp.cats_effects.actors.model.Context

import scala.concurrent.duration._

class HelloActor(
  val semaphore: Semaphore[IO],
  val name: String
)(
  implicit timer: Timer[IO],
  val contextShift: ContextShift[IO]
) extends Actor_2[String] {
  override protected def handleMessage[SenderIn]: (IO[String], IO[Context[SenderIn]], IO[String => SenderIn], IO[SenderIn => String]) => IO[Unit] =
    (entityIO, contextIO, fIO, f1IO) =>
      for {
        entity <- entityIO
        f <- fIO
        _ <- IO(println(s"My name is $name"))
        _ <- IO.sleep(1.second)
        context <- contextIO
        _ <- context.sender.sendMessage(IO(f(entity)), IO(Context(this)), f1IO, fIO)
      } yield ()

}

object HelloActor {
  def apply(
    semaphore: Semaphore[IO],
    name: String
  )(
    implicit timer: Timer[IO],
    contextShift: ContextShift[IO]
  ): HelloActor =
    new HelloActor(semaphore, name)
}
package com.evolutiongaming.bootcamp.cats_effects.actors

import cats.effect.IO
import cats.effect.kernel.Temporal
import cats.effect.std.Semaphore
import com.evolutiongaming.bootcamp.cats_effects.actors.model.Context

import scala.concurrent.duration._

class HelloActor(
  val semaphore: Semaphore[IO],
  val name: String,
)(implicit
  timer: Temporal[IO]
) extends Actor_2[String] {
  override protected def handleMessage[SenderIn]
    : (IO[String], IO[Context[SenderIn]], IO[String => SenderIn], IO[SenderIn => String]) => IO[Unit] =
    (entityIO, contextIO, fIO, f1IO) =>
      for {
        entity  <- entityIO
        f       <- fIO
        _       <- IO(println(s"My name is $name"))
        _       <- IO.sleep(1.second)
        context <- contextIO
        _       <- context.sender.sendMessage(IO(f(entity)), IO(Context(this)), f1IO, fIO)
      } yield ()

}

object HelloActor {
  def apply(
    semaphore: Semaphore[IO],
    name: String,
  )(implicit
    timer: Temporal[IO]
  ): HelloActor =
    new HelloActor(semaphore, name)
}

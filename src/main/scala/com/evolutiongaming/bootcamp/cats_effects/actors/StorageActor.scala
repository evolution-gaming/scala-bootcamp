package com.evolutiongaming.bootcamp.cats_effects.actors

import cats.effect.concurrent.Semaphore
import cats.effect.{ContextShift, IO, Timer}
import cats.implicits._

import scala.concurrent.duration._

class StorageActor(
  val semaphore: Semaphore[IO]
)(
  implicit timer: Timer[IO],
  val contextShift: ContextShift[IO]
) extends AskActor[Int, Int] {
  override def name: String = "storage_actor"
  @volatile private var account = 0

  override protected def handleMessage: Int => IO[Unit] = {
    case v => IO {
      account = account + v
      println(account)
    } *> IO.sleep(5.second) *> IO(println("END!!!"))
  }

  override protected def handleMessageWithResult: Int => IO[Int] = {
    case v => IO {
      account = account + v
      println(account)
      account
    } <* IO.sleep(5.second) <* IO(println("END!!!"))
  }
}

object StorageActor {
  def apply(
    semaphore: Semaphore[IO]
  )(
    implicit timer: Timer[IO],
    contextShift: ContextShift[IO]
  ): StorageActor =
    new StorageActor(semaphore)
}

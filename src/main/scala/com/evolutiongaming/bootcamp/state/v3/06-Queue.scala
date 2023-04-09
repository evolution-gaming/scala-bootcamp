package com.evolutiongaming.bootcamp.state.v3

import cats.effect.std.Queue
import cats.effect.{IO, IOApp}
import scala.concurrent.duration._
import cats.syntax.all._

/** Cats Effect also has a pure, concurrent implementation of a queue. */
object QueueDemo extends IOApp.Simple {
  def producer(n: Int, delay: FiniteDuration, queue: Queue[IO, Int]): IO[Unit] =
    List.range(0, n).traverse(n => queue.offer(n).delayBy(delay)) *> queue.offer(-1) *> IO.println("producer finished")

  def consumer(delay: FiniteDuration, queue: Queue[IO, Int]): IO[Unit] = queue.take.flatMap {
    case -1 => IO.println("consumer finished")
    case n  => IO.println(s"consumed: $n") *> consumer(delay, queue).delayBy(delay)
  }

  override def run: IO[Unit] =
    for {
      queue  <- Queue.unbounded[IO, Int]
      handle <- producer(50, 10.millis, queue).start
      _      <- consumer(30.millis, queue)
      _      <- handle.join
    } yield ()

  // question: how the bounded queue would behave?
}

/** Cats effect std library has many other "pure" and thread-safe concurrency primitives:
  * Count Down Latch, Dequeue, and more. Refer to documentation:
  *
  * @see https://typelevel.org/cats-effect/docs/getting-started
  */

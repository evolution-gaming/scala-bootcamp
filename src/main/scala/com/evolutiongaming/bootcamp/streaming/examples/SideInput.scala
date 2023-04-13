package com.evolutiongaming.bootcamp.streaming.examples

import cats.effect.kernel.Resource
import cats.effect.std.Queue
import cats.effect.{IO, IOApp}
import cats.syntax.traverse._
import fs2.Stream

import scala.concurrent.duration.DurationInt

object SideInput extends IOApp.Simple {
  // Side input into a stream
  // There is a stream running in background, you want to insert elements there
  // Solution: use a queue
  val streamWithInput = for {
    input <- Resource.eval(Queue.bounded[IO, Option[Int]](10))

    // Background stream
    streamFiber <- Stream
      .fromQueueNoneTerminated(input) // this is Stream[IO, Int]
      .evalMap(value => IO(println(s"Received $value")))
      .onFinalize(IO(println("Terminating")))
      .compile
      .drain
      .background

    // Feed it some elements
    _           <- Resource.eval(
      (1 to 5).toList.traverse(toInsert => input.offer(Some(toInsert)))
    )
    // Stop the stream by sending None
    _           <- Resource.eval(input.offer(None))
    // Await stream end
    _           <- Resource.eval(streamFiber)
  } yield ()

  def run: IO[Unit] = streamWithInput.use(_ => IO.unit).timeout(10.seconds)
}

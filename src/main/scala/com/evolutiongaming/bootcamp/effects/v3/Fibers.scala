package com.evolutiongaming.bootcamp.effects.v3

import cats.effect.{ExitCode, IO, IOApp}

import scala.concurrent.duration._

/*
  https://typelevel.org/cats-effect/docs/2.x/datatypes/fiber
  It represents the (pure) result of an Async data type (e.g. IO) being started concurrently and that can be either joined or canceled.
  You can think of fibers as being lightweight threads, a fiber being a concurrency primitive for doing cooperative multi-tasking.

  `Fiber` is just a handle over a runloop
  `join`: semantically blocks for completion (via `Deferred` and ultimately `Ref` + `async`)
  `cancel`: interruption (runloop stops running on a signal, out of scope)
 */
object Fibers extends IOApp {

  def logLine(s: => String): IO[Unit] = IO.suspend(IO.delay(println(s"${Thread.currentThread().toString} $s")))

//  val io: IO[Unit] = (IO.sleep(10.seconds) *> logLine("Delayed")) // Fiber[IO, Unit]

  def run(args: List[String]): IO[ExitCode] =
    for {
      _ <- logLine("Started")
      fiber <- (IO.sleep(10.seconds) *> logLine("Delayed")).start
      _ <- fiber.join
      _ <- logLine("Finished")
    } yield ExitCode.Success
}

package com.evolutiongaming.bootcamp.state.v3

import cats.effect.kernel.Deferred
import cats.effect.{IO, IOApp}

import scala.concurrent.duration.DurationInt

/** Other Cats-Effect concurrency primitives */

/** Deferred.
  *
  * Just as `Ref` is a "pure" alternative to `AtomicReference`, `Deferred` is a "pure" alternative to `Promise`.
  * It is a synchronisation primitive that represents a single value which may not yet be available.
  *
  * abstract class Deferred[F[_], A] {
  *  def get: F[A]
  *  def complete(a: A): F[Boolean]
  * }
  *
  * It is first created empty and then can be completed only once. Calling `get` on an empty `Deferred` blocks until
  * it is completed. The blocking is semantic only, no actual threads are blocked.
  * Once it is completed, it will stay that way.
  *
  * @see https://typelevel.org/cats-effect/docs/std/deferred
  */
object DeferredDemo extends IOApp.Simple {

  override def run: IO[Unit] =
    for {
      deferred <- Deferred[IO, Int]
      _        <- (IO.sleep(3.seconds) *> deferred.complete(0)).start
      _        <- IO.println("waiting for deferred to complete")
      result   <- deferred.get
      _        <- IO.println(s"result: $result")
    } yield ()
  // what happens if deferred is completed for the 2nd time?

  // exercise: implement two fibers trying to complete a deferred at the same time
  // hint: you can use start or parTupled to start the fibers
}

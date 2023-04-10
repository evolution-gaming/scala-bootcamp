package com.evolutiongaming.bootcamp.state.v3

import cats.effect.std.{Mutex, Semaphore}
import cats.effect.{IO, IOApp}

import scala.concurrent.duration._
import cats.syntax.all._

/** Semaphore is another synchronization primitive - a "pure" alternative to Java's semaphore.
  * It maintains a set of permits and provides an api to acquire/release them. A call to `acquire` semantically blocks
  * if there are no permits available. A call to `release` adds a permit potentially releasing a blocked acquirer.
  */
object SemaphoreDemo extends IOApp.Simple {
  case class PreciousResource private (sem: Semaphore[IO]) {
    def use(name: String): IO[Unit] =
      for {
        _ <- sem.acquire
        _ <- IO.println(s"$name started")
        _ <- IO.sleep(3.seconds)
        _ <- sem.release
        _ <- IO.println(s"$name done")
      } yield ()
  }

  object PreciousResource {
    def create(permits: Int): IO[PreciousResource] = Semaphore[IO](permits).map(PreciousResource(_))
  }

  override def run: IO[Unit] =
    for {
      resource <- PreciousResource.create(3)
      _        <- List.range(0, 10).map(_.toString).parTraverse(resource.use)
    } yield ()
}

object RateLimiterDemo extends IOApp.Simple {
  def createWorker(name: String) =
    IO.println(s"worker $name started") *> IO.sleep(3.second) *> IO.println(
      s"worker $name finished"
    )

  trait RateLimiter {
    def apply[A](fa: IO[A]): IO[A]
  }

  object RateLimiter {
    def create(permits: Int): IO[RateLimiter] = Semaphore[IO](permits).map { sem =>
      new RateLimiter {
        override def apply[A](fa: IO[A]): IO[A] = sem.permit.surround(fa)
      }
    }
  }

  override def run: IO[Unit] =
    for {
      rateLimiter <- RateLimiter.create(3)
      _           <- List
        .range(0, 10)
        .map(_.toString)
        .map(createWorker)
        .parTraverse(rateLimiter.apply)
    } yield ()
}

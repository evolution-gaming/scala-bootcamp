package com.evolutiongaming.bootcamp.state.v3

import cats.Monad
import cats.effect._

import scala.concurrent.duration._

/*
 * Please implement a Cache which allows concurrent access.
 *
 * Tip: checking expiration could be represented as some infinite process somewhere in background
 *
 * Cached items should have an expiration timestamp after which they are evicted.
 */
object ExpiringCacheExercise extends IOApp {

  trait Cache[F[_], K, V] {
    def get(key: K): F[Option[V]]

    def put(key: K, value: V): F[Unit]
  }

  class RefCache[F[_]: Clock: Monad, K, V](
    state: Ref[F, Map[K, (Long, V)]],
    expiresIn: FiniteDuration,
  ) extends Cache[F, K, V] {

    def get(key: K): F[Option[V]] = ???

    def put(key: K, value: V): F[Unit] = ???

  }

  object Cache {
    def of[F[_]: Clock: Temporal, K, V](
      expiresIn: FiniteDuration,
      checkOnExpirationsEvery: FiniteDuration,
    ): F[Cache[F, K, V]] = ???
    // depending on approach Resource[F, Cache[F, K, V]] might be also an option to return here e.g. to use .background operator to start a fiber
    // that'll check on cache expiration
  }

  override def run(args: List[String]): IO[ExitCode] =
    for {
      cache <- Cache.of[IO, Int, String](10.seconds, 4.seconds)
      _     <- cache.put(1, "Hello")
      _     <- cache.put(2, "World")
      _     <- cache
        .get(1)
        .flatMap(s =>
          IO {
            println(s"first key $s")
          }
        )
      _     <- cache
        .get(2)
        .flatMap(s =>
          IO {
            println(s"second key $s")
          }
        )
      _     <- IO.sleep(12.seconds)
      _     <- cache
        .get(1)
        .flatMap(s =>
          IO {
            println(s"first key $s")
          }
        )
      _     <- cache
        .get(2)
        .flatMap(s =>
          IO {
            println(s"second key $s")
          }
        )
      _     <- IO.sleep(12.seconds)
      _     <- cache
        .get(1)
        .flatMap(s =>
          IO {
            println(s"first key $s")
          }
        )
      _     <- cache
        .get(2)
        .flatMap(s =>
          IO {
            println(s"second key $s")
          }
        )
    } yield ExitCode.Success
}

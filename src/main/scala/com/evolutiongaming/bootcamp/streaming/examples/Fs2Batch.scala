package com.evolutiongaming.bootcamp.streaming.examples

import cats.effect._
import cats.implicits._
import fs2.{Pipe, Stream}

import java.time.Instant
import scala.concurrent.duration.DurationInt

object Fs2Batch extends IOApp.Simple {

  /* Inspired by akka's Flow.batch
   * Based on Fabio Labella's answer https://github.com/typelevel/fs2/issues/2019#issuecomment-685766344
   * TODO consider max = 0 and verify it behaves like identity
   */
  def batch[F[_]: Async, O, S](max: Int, seed: O => S)(aggregate: (S, O) => S): Pipe[F, O, S] = { in =>
    sealed trait State
    final case object Empty                                                                      extends State
    final case class Accumulating(batch: S, count: Int, backpressure: Option[Deferred[F, Unit]]) extends State
    final case class WaitingFromUpstream(deferred: Deferred[F, Option[S]])                       extends State
    final case class Stopped(lastBatch: Option[S])                                               extends State

    Stream.eval(Ref.of[F, State](Empty)).flatMap { q =>
      def enqueue(v: Option[O]): F[Unit] = Deferred[F, Unit].flatMap { backpressure =>
        q.modify {
          case Empty                                                  =>
            v match {
              case Some(value) => Accumulating(seed(value), 1, none) -> ().pure[F]
              case None        => Stopped(none)                      -> ().pure[F]
            }
          case acc @ Accumulating(batch, count, previousBackpressure) =>
            previousBackpressure match {
              case None if count < max =>
                v match {
                  case Some(value) => Accumulating(aggregate(batch, value), count + 1, none) -> ().pure[F]
                  case None        => Stopped(batch.some)                                    -> ().pure[F]
                }
              case None    => acc.copy(backpressure = backpressure.some) -> backpressure.get.flatMap(_ => enqueue(v))
              case Some(_) =>
                acc -> new RuntimeException(
                  "unexpected state of backpressure, next element can't be processed when previous one was backpressured"
                )
                  .raiseError[F, Unit]
            }
          case WaitingFromUpstream(deferred)                          => Empty -> deferred.complete(v.map(seed)).void
          case s: Stopped                                             =>
            s -> new RuntimeException(
              "unexpected state, expected: Empty, Accumulating or WaitingFromUpstream, got: Stopped"
            ).raiseError[F, Unit]
        }
      }.flatten

      def dequeue: F[List[Option[S]]] = Deferred[F, Option[S]].flatMap { deferred =>
        q.modify {
          case Empty                                => WaitingFromUpstream(deferred) -> deferred.get.map(_ :: Nil)
          case Accumulating(batch, _, backpressure) =>
            Empty -> backpressure.foldMapM(_.complete(()).void).as(batch.some :: Nil)
          case w: WaitingFromUpstream               =>
            w -> new RuntimeException(
              "unexpected state, expected: Empty or Accumulating, got: WaitingFromUpstream"
            ).raiseError[F, List[Option[S]]]
          case Stopped(lastBatch) => Empty -> lastBatch.map(_.some :: none :: Nil).getOrElse(none :: Nil).pure[F]
        }.flatten
      }

      // In case producer terminated, we stop consumer via publishing None
      val producer = in.evalMap(elem => enqueue(elem.some)).onFinalize(enqueue(none))
      val consumer = Stream.repeatEval(dequeue).flatMap(Stream.emits).unNoneTerminate

      consumer.concurrently(producer)
    }
  }

  def run: IO[Unit] = {
    Stream
      .range(0, 100, 1)
      .evalMap(i => IO.delay { println(s"${Instant.now} emit: $i") }.as(i))
      .meteredStartImmediately[IO](10.millis)
      .through(batch[IO, Int, List[Int]](20, _ :: Nil) { (acc, next) => next :: acc })
      .evalMap(input => IO.println(s"${Instant.now} outer: ${input.reverse}") *> IO.sleep(35.millis))
      .compile
      .drain
  }
}

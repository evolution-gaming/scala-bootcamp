package com.evolutiongaming.bootcamp.state.v3

import cats.effect.kernel.Ref
import cats.effect.unsafe.implicits.global
import cats.effect.{IO, IOApp}
import cats.syntax.all._

import java.util
import java.util.concurrent.ConcurrentHashMap
import scala.collection.{immutable, mutable}

/** cats-effect Ref is a thread-safe functional reference
  * But can we safely use it with both mutable and immutable state?
  */
object RefDemo extends IOApp.Simple {
  trait EventLog {
    def append(event: String): IO[Unit]
    def count: IO[Int]
  }

  /** Using mutable data structures in Ref is not thread safe
    * Ref uses compare-and-swap under the hood which compares underlying references. if we mutate the state, the reference stays the same
    * if we need to use mutable data structures, we need to choose thread-safe implementations like ConcurrentHashMap from java API
    */
  val mutableStateRef: IO[EventLog] =
    Ref.of[IO, scala.collection.mutable.Map[String, Int]](scala.collection.mutable.Map.empty[String, Int]).map { ref =>
      new EventLog {
        override def append(event: String): IO[Unit] = ref.update(_.addOne(event -> 42))

        override def count: IO[Int] = ref.get.map(_.size)
      }
    }

  /** we can safely use Ref with immutable data structures */
  // exercise: implement event log with the Vector from scala immutable collection library
  val immutableStateRef: IO[EventLog] = ???

  def parAppendEventLog(eventLog: IO[EventLog]) =
    for {
      ref   <- eventLog
      _     <- List.range(0, 1000).map(_.toString).parTraverse(ref.append)
      count <- ref.count
      _     <- IO.println(s"count: $count")
    } yield ()

  override def run: IO[Unit] = parAppendEventLog(mutableStateRef)
}

package com.evolutiongaming.bootcamp.state.v3

import cats.effect.kernel.Ref
import cats.effect.{IO, IOApp}
import cats.syntax.all._

import java.util.concurrent.atomic.{AtomicInteger, AtomicReference}

/** Java API comes with classes such as AtomicReference, AtomicInteger, AtomicLong, etc.
  * They guarantee atomic updates to the underlying objects or values.
  * In scala we don't usually use them directly, we rather use APIs from cats-effect, ZIO, etc. that use those Atomic java classes under the hood.
  * Let's see how we can implement purely functional wrapper around AtomicReference.
  */
object AtomicRef extends IOApp.Simple {
  trait AtomicRef[A] {
    def get: IO[A]
    def set(value: A): IO[Unit]
    def update(f: A => A): IO[Unit] // will require 'optimistic lock'
    def modify[B](f: A => (A, B)): IO[B]
  }

  def createAtomicRef[A](initialValue: A): IO[AtomicRef[A]] = IO(new AtomicReference[A](initialValue)).map { ref =>
    new AtomicRef[A] {
      override def get: IO[A] = IO(ref.get())

      override def set(value: A): IO[Unit] = IO(ref.set(value))

      override def update(f: A => A): IO[Unit] = {
        def loop(): Unit = {
          val prev = ref.get()
          val next = f(prev)
          if (ref.compareAndSet(prev, next)) ()
          else loop()
        }
        IO(loop())
      }

      override def modify[B](f: A => (A, B)): IO[B] = {
        def loop(): B = {
          val prev           = ref.get()
          val (next, result) = f(prev)
          if (ref.compareAndSet(prev, next)) result
          else loop()
        }
        IO(loop())
      }
    }
  }

  // here we're starting 3 fibers, each fiber will increment ref 3 times
  // we'll end up with ref = 9 but there'll be more than 9 operations
  // fiber 1: x += 1, x += 1, x += 1
  // fiber 2: x += 1, x += 1, x += 1
  // fiber 3: x += 1, x += 1, x += 1
  def modifyAndPrint(ref: AtomicRef[Int]) =
    List
      .fill(3)(0)
      .parTraverse(_ =>
        ref
          .modify(x =>
            (x + 1, println(s"prev=$x, next=${x + 1}"))
          ) // println is 'eager' so it'll be run even if function won't return it
          .replicateA(3)
      )
      .void

  // if we delay println, we'll end up just with println returned by our function
  def modifyAndIOPrint(ref: AtomicRef[Int]) =
    List
      .fill(3)(0)
      .parTraverse(_ =>
        ref
          .modify(x => (x + 1, IO.println(s"prev=$x, next=${x + 1}")))
          .flatten
          .replicateA(3)
      )
      .void

  def compareAndSetDemo(modifyRef: AtomicRef[Int] => IO[Unit]) =
    for {
      ref    <- createAtomicRef(0)
      _      <- modifyRef(ref)
      result <- ref.get
      _      <- IO.println(s"result: $result")
    } yield ()

  override def run: IO[Unit] = compareAndSetDemo(modifyAndIOPrint)
}

object AtomicRefCounter extends IOApp.Simple {
  import AtomicRef._

  trait Counter {
    def inc: IO[Unit]
    def get: IO[Int]
  }

  /** use AtomicRef to implement counter */
  def makeAtomicRefCounter: IO[Counter] = createAtomicRef(0).map { ref =>
    new Counter {
      override def inc: IO[Unit] = ref.update(_ + 1)

      override def get: IO[Int] = ref.get
    }
  }

  // we can also use java concurrent API to create thread-safe counter
  /** use AtomicInteger from java API to implement counter */
  def makeAtomicIntCounter: IO[Counter] = IO(new AtomicInteger(0)).map { ref =>
    new Counter {
      override def inc: IO[Unit] = IO(ref.updateAndGet(_ + 1))

      override def get: IO[Int] = IO(ref.get)
    }
  }

  // cats-effect provides Ref, thread-safe reference
  /** use cats effect Ref to implement counter */
  def makeRefCounter: IO[Counter] = Ref.of[IO, Int](0).map { ref =>
    new Counter {
      override def inc: IO[Unit] = ref.update(_ + 1)

      override def get: IO[Int] = ref.get
    }
  }

  override def run: IO[Unit] =
    for {
      counter <- makeRefCounter
      _       <- List.fill(1000)(0).parTraverse(_ => counter.inc)
      result  <- counter.get
      _       <- IO.println(s"result: $result")
    } yield ()
}

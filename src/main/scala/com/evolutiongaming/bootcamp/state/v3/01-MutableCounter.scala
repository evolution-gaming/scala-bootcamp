package com.evolutiongaming.bootcamp.state.v3

import cats.data.State
import cats.effect.{IO, IOApp}
import cats.effect.unsafe.implicits.global
import cats.syntax.all._

/** In the following examples we'll implement a simple counter that can increment and get a value.
  * We'll try to find purely functional implementation.
  */
object MutableCounter extends App {
  trait Counter {
    def increment: Unit
    def get: Long
  }

  val mutableCounter: Counter = {
    var count = 0
    new Counter {
      override def increment: Unit = count += 1

      override def get: Long = count
    }
  }

  val x  = mutableCounter.get
  val y  = x + x
  mutableCounter.increment
  val y1 = mutableCounter.get + mutableCounter.get

  // this implementation of counter is not RT because it uses mutable shared state
  println(s"y=$y, y1=$y1")
}

/** This representation of counter is purely functional.
  * It's implemented using a pattern that can be used to make non-pure code referentially transparent.
  * 'increment' method will return new reference to the Counter which can be used to compute next value
  */
object ImmutableCounter extends App {
  trait Counter {
    def increment: Counter
    def get: Long
  }

  def makeCounter(initial: Long): Counter = new Counter {
    override def increment: Counter = makeCounter(initial + 1)

    override def get: Long = initial
  }

  val counter  = makeCounter(0)
  val counter1 = counter.increment
  val x        = counter1.get
  val y        = x + x
  counter1.increment
  val y1       = counter1.get + counter1.get

  println(s"y=$y, y1=$y1")

  /** implementation of counter can be used with cats State monad.
    */
  val incrementAndGet: State[Counter, Long] =
    State[Counter, Long](counter => (counter.increment, counter.get))

  /** Below program increments counter 3 times.
    * runS is supplied with initial counter and returns final state which is the counter that was incremented 3 times.
    */
  val result = (for {
    _ <- incrementAndGet
    _ <- incrementAndGet
    _ <- incrementAndGet
  } yield ()).runS(makeCounter(0)).value

  println(s"result: ${result.get}")
}

/** exercise: using the pattern from last example, implement pseudo random number generator
  */
object PseudoRandomNumberGenerator extends App {
  // linear congruential generator
  case class Seed(long: Long) {
    def next: Seed = Seed(long * 6364136223846793005L + 1442695040888963407L)
  }

  trait Random {
    def nextLong: (Random, Long)
  }

  def createRandom(seed: Seed): Random = new Random {
    override def nextLong: (Random, Long) = (createRandom(seed.next), seed.next.long)
  }

  val random = createRandom(Seed(0))

  val (random1, long1) = random.nextLong
  val (random2, long2) = random1.nextLong

  println(s"long1=$long1, long2=$long2")
}

/** IO monad from cats-effect can be used to make our implementation of counter purely functional,
  * without the need to keep track of multiple counter reference like in previous example.
  * Access, modification and creation of mutable state needs to be suspended in IO.
  */
object IOCounter extends App {
  trait Counter {
    def increment: IO[Unit]
    def get: IO[Long]
  }

  val unsafeCounter: Counter = {
    var count = 0
    new Counter {
      override def increment: IO[Unit] = IO(count += 1)

      override def get: IO[Long] = IO(count)
    }
  }

  val leakyStateExample = (unsafeCounter.increment *> unsafeCounter.get
    .debug() *> unsafeCounter.increment *> unsafeCounter.get.debug())
    .unsafeRunSync()

  /** Here we suspend creating mutable state in IO, which makes this counter implementation pure.
    */
  def safeCounter: IO[Counter] = IO {
    var count = 0
    new Counter {
      override def increment: IO[Unit] = IO(count += 1)

      override def get: IO[Long] = IO(count)
    }
  }

  val safeCounterExample = safeCounter
    .flatMap { counter =>
      counter.increment *> counter.increment *> counter.get.debug().void
    }

  /** flatMap defines region of sharing. It's the same as call graph.
    * We're explicit about which parts of our program share the same instance of counter.
    */
  val regionsOfSharingExample = safeCounter.flatMap { counter =>
    counter.increment *> counter.get.debug()
  } *> safeCounter.flatMap { counter =>
    counter.increment *> counter.get.debug()
  }
}

/** So far we've found a way to represent state in a purely functional way.
  * But the solution is not perfect yet - we need the state to be thread-safe.
  * Below is the example of the problem that we'll encounter while using current state implementation in concurrent program.
  */
object CounterConcurrencyDemo extends IOApp.Simple {
  // every count incrementation consists of 3 operations: read the count, increment, write back
  // by default incrementation is not atomic

  // T1: reads count: 0
  // T1: calculates new value: 1
  // T2: reads count: 0
  // T1: writes new value: 1
  // T2: calculates new value: 1
  // T2: writes new value: 1
  // we end up with count value 1 instead of 2

  // in the following example with end up with a race condition
  override def run: IO[Unit] =
    for {
      counter <- IOCounter.safeCounter
      _       <- List.fill(1000)(0).parTraverse(_ => counter.increment)
      result  <- counter.get
      _       <- IO.println(s"result: $result")
    } yield ()
}

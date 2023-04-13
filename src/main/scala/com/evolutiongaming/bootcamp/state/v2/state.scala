package com.evolutiongaming.bootcamp.state.v2

import cats.Monad
import cats.effect._
import cats.effect.std.{Queue, Semaphore}
import cats.implicits.toTraverseOps
import cats.instances.list._
import cats.syntax.parallel._
import zio.{Unsafe, ZIO}
import zio.stm.{STM, TRef}

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference
import scala.annotation.tailrec
import scala.concurrent.duration._

/** State is an overloaded term, but here we will say it is any variable, object or memory space that exists in our program.
  * Note: we are only talking about in-memory state, not DB, FS etc.
  *
  * State can be looked at from multiple angles:
  *
  *  - mutable vs immutable
  *  - local vs shared
  *
  * Local state is the one that is only accessible from a single function scope.
  * If an object is accessible from a broader than local scope, it is considered shared state.
  *
  * Our goals:
  *  - Referential Transparency (aka purity)
  *  - Thread Safety (since shared state implies concurrency)
  */

/** Referential transparency is the core of pure FP, it enables:
  *  - substitution model
  *  - functional composition (no statements, only expressions)
  *  - local reasoning
  *
  * Side effects break referential transparency
  */
object ReferentialTransparencyDemo extends App {
  // this is pure
  val x  = "hello".reverse
  val y  = x ++ x
  val y1 = "hello".reverse ++ "hello".reverse
  println(s"y = $y, y1 = $y1")
}

/** Immutable shared state is referentially transparent.
  */
object ImmutableSharedStateDemo extends App {
  val ratings = Map(
    "foo" -> 5,
    "bar" -> 7,
  )

  val foo    = ratings("foo")
  val bar    = ratings("bar")
  val total  = foo + bar
  val total1 = ratings("foo") + ratings("bar")
  println(s"total=$total, total1=$total1")
}

/** Mutable local state is fine as well, it is referentially transparent from the outside.
  */
object MutableLocalStateDemo extends App {

  def pureMap[A, B](as: List[A])(f: A => B): List[B] =
    as match {
      case ::(head, tail) => f(head) :: pureMap(tail)(f)
      case Nil            => Nil
    }

  def impureMap[A, B](as: List[A])(f: A => B): List[B] = {
    val buffer = new scala.collection.mutable.ArrayBuffer[B]
    for (a <- as)
      buffer.addOne(f(a))
    buffer.toList
  }

  def duplicate(s: String): String = s ++ s

  val xs             = List("foo", "bar")
  val mappedPurely   = pureMap(xs)(duplicate)
  val mappedImpurely = impureMap(xs)(duplicate)

  println(s"mappedPurely=$mappedPurely, mappedImpurely=$mappedImpurely")
}

/** Mutating shared state is not referentially transparent, because it is a side effect (but doesn't have to be).
  */
object MutableSharedStateDemo extends App {
  class Counter {
    private var count: Long = 0

    def incrementAndGet(): Long = {
      count += 1
      count
    }
  }

  val counter = new Counter
  val x       = counter.incrementAndGet()
  val y       = x + x
  val y1      = counter.incrementAndGet() + counter.incrementAndGet()
  println(s"y=$y, y1=$y1")
}

/** The solution - suspend the side effect by wrapping it with some effect type (e.g. IO, ZIO, but not Future).
  *
  * Rule: access, modification and creation of mutable state needs suspension in effect.
  */

trait Counter[F[_]] {
  def inc: F[Unit]

  def get: F[Long]
}

object Counter {
  // the outer IO wrapping is necessary to suspend creation of mutable variable `count`
  /*def create: IO[Counter[IO]] = IO {
    var count: AtomicLong = new AtomicLong(0)

    new Counter[IO] {
      override def inc: IO[Unit] = IO(count.incrementAndGet())

      override def get: IO[Long] = IO(count.get())
    }
  }*/
  def create: IO[Counter[IO]] =
    Ref.of[IO, Long](0).map { ref =>
      new Counter[IO] {
        override def inc: IO[Unit] = ref.update(_ + 1)

        override def get: IO[Long] = ref.get
      }
    }

  def createUnsafe: Counter[IO] =
    new Counter[IO] {
      var count: Long = 0

      override def inc: IO[Unit] = IO(count += 1)

      override def get: IO[Long] = IO(count)
    }
}

object CounterDemo extends IOApp {
  val counter = Counter.create

  // Question 0: what does it print?
  val program = for {
    _ <- counter.flatMap(_.inc)
    c <- counter.flatMap(_.get)
    _ <- IO(println(c))
  } yield ()

  override def run(args: List[String]): IO[ExitCode] =
    program.as(ExitCode.Success)
}

object StateSharingAndIsolationDemo extends App {
  import cats.effect.unsafe.implicits.global

  def program1(counter: Counter[IO]): IO[Unit] =
    counter.inc

  def program2(name: String)(counter: Counter[IO]): IO[Unit] =
    for {
      c <- counter.get
      _ <- IO(println(s"$name, counter is: $c"))
    } yield ()

  val counter = Counter.create

  val sharingSameCounter =
    counter.flatMap { counter =>
      program1(counter) *> program2("Sharing")(counter)
    }

  val separateCounters =
    counter.flatMap(program1) *> counter.flatMap(program2("Separate"))

  val program = sharingSameCounter *> separateCounters

  program.unsafeRunSync()

  /** !The regions of state sharing are the same as the call graph!
    *
    * The state is being shared simply by passing arguments down the call stack, so we are very explicit about which state
    * is shared and which is not. There are no global singletons or other crap.
    *
    * Calling `flatMap` on a state "constructor" creates a region of sharing.
    */
}

object LeakyStateDemo extends App {
  import cats.effect.unsafe.implicits.global

  val counter = Counter.createUnsafe

  val x  = counter.inc *> counter.get
  val x1 = Counter.createUnsafe.inc *> Counter.createUnsafe.get

  val program = for {
    y  <- x
    y1 <- x1
    _  <- IO(println(s"y=$y, y1=$y1"))
  } yield ()

  program.unsafeRunSync()
}

object CounterConcurrencyDemo extends IOApp {

  def incTimes(n: Int)(counter: Counter[IO]): IO[Unit] =
    List.fill(n)(counter.inc).parSequence.void

  override def run(args: List[String]): IO[ExitCode] =
    for {
      counter <- Counter.create
      _       <- incTimes(10000)(counter)
      c       <- counter.get
      _       <- IO(println(c))
    } yield ExitCode.Success
}

/** Looks like we have a race condition. The reason is that `count += 1` is not an atomic operation,
  * it is in fact 3 operations:
  *
  * - read value of count
  * - increment the value
  * - write the value back
  *
  * Multiple threads can get interleaved in different ways while execution these steps, example (count is 5):
  * T1: read current value => 5
  * T2: read current value => 5
  * T1: increment the value => 6
  * T2: increment the value => 6
  * T1: write the value => 6
  * T2: write the value => 6
  *
  * Question 1: how do we fix it?
  */

/** We've made shared state mutations pure, but it feels kinda "hacky" to always manually wrap working with state in effects.
  * Can we do better?
  * What if implement a pure wrapper around an `AtomicReference` and use it instead? That way we won't have to manually
  * wrap anything inside `Counter`.
  */
trait AtomicRef[F[_], A] {
  def get: F[A]

  def set(a: A): F[Unit]

  def update(f: A => A): F[Unit]
}

object AtomicRef {
  // Exercise 0: let's implement it

  import cats.syntax.functor._

  def create[F[_]: Sync, A](initialValue: A): F[AtomicRef[F, A]] =
    Sync[F].delay(new AtomicReference[A](initialValue)).map { ref =>
      new AtomicRef[F, A] {
        override def get: F[A] = Sync[F].delay(ref.get())

        override def set(a: A): F[Unit] = Sync[F].delay(ref.set(a))

        override def update(f: A => A): F[Unit] = {
          @tailrec
          def loop(): Unit = {
            val current = ref.get()
            val next    = f(current)
            if (ref.compareAndSet(current, next)) ()
            else loop()
          }

          Sync[F].delay(loop())
        }
        // Sync[F].delay(ref.updateAndGet(a => f(a))).void
      }
    }
}

object MutableVsImmutableStateDemo extends IOApp {

  /** Should the state "inside" `Ref` be mutable or immutable? Does it matter?
    */

  trait EventLog[F[_]] {
    def append(event: String): F[Unit]

    def count: F[Int]
  }

  val mutableEventLog: IO[EventLog[IO]] =
    Ref.of[IO, ConcurrentHashMap[String, Int]](new ConcurrentHashMap[String, Int]()).map { ref =>
      new EventLog[IO] {
        override def append(event: String): IO[Unit] = ref.update { map =>
          map.put(event, 42)
          map
        }

        override def count: IO[Int] = ref.get.map(_.size)
      }
    }

  val immutableEventLog: IO[EventLog[IO]] =
    Ref.of[IO, Vector[String]](Vector.empty).map { ref =>
      new EventLog[IO] {
        override def append(event: String): IO[Unit] = ref.update(_ :+ event)

        override def count: IO[Int] = ref.get.map(_.size)
      }
    }

  def logEvents(n: Int)(log: EventLog[IO]): IO[Unit] =
    List.range(0, n).map(i => log.append(s"event nr. $i")).parSequence.void

  def logAndCountEvents(name: String)(log: EventLog[IO]): IO[Unit] =
    for {
      _ <- logEvents(10000)(log)
      c <- log.count
      _ <- IO(println(s"$name: $c"))
    } yield ()

  // Question 2: which is correct? Mutable, immutable, or both?

  override def run(args: List[String]): IO[ExitCode] =
    for {
      _ <- immutableEventLog.flatMap(logAndCountEvents("Immutable"))
      _ <- mutableEventLog.flatMap(logAndCountEvents("Mutable"))
    } yield ExitCode.Success
}

object RefAccessDemo {
  def update[F[_]: Monad, A](ref: Ref[F, A])(f: A => F[A]): F[A] = ???
}

/** Other Cats-Effect concurrency primitives */

/** Deferred.
  *
  * Just as `Ref` is a "pure" alternative to `AtomicReference`, `Deferred` is a "pure" alternative to `Promise`.
  * It is a synchronisation primitive represents a single value which may not yet be available.
  *
  * abstract class Deferred[F[_], A] {
  * def get: F[A]
  * def complete(a: A): F[Boolean]
  * }
  *
  * It is first created empty and then can be completed only once. Calling `get` on an empty `Deferred` blocks until
  * it is completed. The blocking is semantic only, no actual threads are blocked.
  * Once it is completed, it will stay that way.
  *
  * @see https://typelevel.org/cats-effect/docs/std/deferred
  */
object DeferredDemo extends IOApp {

  def completeInThreeSeconds(deferred: Deferred[IO, String]): IO[Unit] =
    (IO.sleep(3.seconds) *> deferred.complete("Hello!")).start.void

  override def run(args: List[String]): IO[ExitCode] =
    for {
      deferred <- Deferred[IO, String]
      _        <- completeInThreeSeconds(deferred)
      _        <- IO(println("Waiting for deferred to complete..."))
      result   <- deferred.get
      _        <- IO(println(s"Deferred completed: $result"))
    } yield ExitCode.Success
}

/** Semaphore is another synchronization primitive - a "pure" alternative to Java's semaphore.
  * It maintains a set of permits and provides an api to acquire/release them. A call to `acquire` semantically blocks
  * it there are no permits available. A call to `release` adds a permit potentially releasing a blocked acquirer.
  */
object SemaphoreDemo extends IOApp {

  class PreciousResource private (sem: Semaphore[IO]) {
    def use(name: String): IO[Unit] =
      for {
        _ <- sem.acquire
        _ <- IO(println(s"$name >> Started"))
        _ <- IO.sleep(3.seconds)
        _ <- IO(println(s"$name >> Done"))
        _ <- sem.release
      } yield ()
  }

  object PreciousResource {
    def create: IO[PreciousResource] =
      Semaphore[IO](3).map(new PreciousResource(_))
  }

  override def run(args: List[String]): IO[ExitCode] =
    for {
      r <- PreciousResource.create
      _ <- List(
        r.use("F1"),
        r.use("F2"),
        r.use("F3"),
      ).parSequence
    } yield ExitCode.Success
}

trait RateLimiter[F[_]] {
  def apply[A](fa: F[A]): F[A]
}

object RateLimiter {
  // Exercise 1: implement it
  def create(n: Long)(implicit C: Concurrent[IO]): IO[RateLimiter[IO]] =
    Semaphore[IO](n).map { sem =>
      new RateLimiter[IO] {
        override def apply[A](fa: IO[A]): IO[A] =
          sem.permit.surround(fa)
      }
    }
}

object RateLimiterDemo extends IOApp {

  def worker(n: Int): IO[Unit] =
    for {
      _ <- IO(println(s"$n >>> starting"))
      _ <- IO.sleep(1000.millis)
      _ <- IO(println(s"$n >>> done"))
    } yield ()

  override def run(args: List[String]): IO[ExitCode] =
    for {
      limiter <- RateLimiter.create(2)
      _       <- List.range(1, 10).map(worker).map(limiter(_)).parSequence.void
    } yield ExitCode.Success
}

/** Cats Effect also has a pure, concurrent implementation of a queue.
  */
object QueueDemo extends IOApp {

  def producer(n: Int, delay: FiniteDuration, queue: Queue[IO, Int]): IO[Unit] =
    List
      .range(1, n)
      .traverse(queue.offer(_).delayBy(delay)) *>
      queue.offer(-1) *>
      IO(println("Producer done"))

  def consumer(delay: FiniteDuration, queue: Queue[IO, Int]): IO[Unit] =
    queue.take.flatMap {
      case -1 => IO(println("Consumer done"))
      case i  => IO(println(s"Consumer got: $i")) *> consumer(delay, queue).delayBy(delay)
    }

  override def run(args: List[String]): IO[ExitCode] =
    for {
      queue <- Queue.bounded[IO, Int](10)
      prod  <- producer(50, 10.millis, queue).start
      _     <- consumer(30.millis, queue)
      _     <- prod.join
    } yield ExitCode.Success
}

/** Cats effect std library has many other "pure" and thread-safe concurrency primitives:
  * Count Down Latch, Dequeue, and more. Refer to documentation:
  *
  * @see https://typelevel.org/cats-effect/docs/getting-started
  */

/** * Software Transactional Memory **
  */

/** We now know how to modify a piece of state atomically.
  * But what if we want to modify multiple pieces of state atomically?
  */
object BankTransferDemo extends IOApp {

  // Exercise 2: make it atomic
  def withdraw(account: Ref[IO, Long], amount: Long): IO[Unit] =
    account.access.flatMap { case (balance, setter) =>
      if (balance < amount) IO.raiseError(new Exception("Insufficient funds"))
      else
        setter(balance - amount).flatMap {
          case true  => IO.pure(())
          case false => withdraw(account, amount)
        }
    }
  /*for {
    balance <- account.get
    _ <- if (balance < amount) IO.raiseError(new Exception("Insufficient funds")) else IO.unit
    _ <- account.update(_ - amount)
  } yield ()*/

  def deposit(account: Ref[IO, Long], amount: Long): IO[Unit] =
    account.update(_ + amount)

  def transfer(from: Ref[IO, Long], to: Ref[IO, Long], amount: Long): IO[Unit] =
    for {
      _ <- withdraw(from, amount)
      _ <- deposit(to, amount)
    } yield ()

  override def run(args: List[String]): IO[ExitCode] =
    for {
      alice <- Ref.of[IO, Long](100)
      bob   <- Ref.of[IO, Long](0)
      _     <- List.fill(10000)(transfer(alice, bob, 1).attempt).parSequence
      ab    <- alice.get
      bb    <- bob.get
      _     <- IO(println(s"Alice: $ab, Bob: $bb"))
    } yield ExitCode.Success
}

/** Possible solutions:
  *  - put all balances under a single `Ref`? (creates contention)
  *  - use pessimistic locking? (would work, but is error-prone and can cause deadlocks)
  *  - use STM
  */

/** Software Transactional Memory (STM) is a modular composable concurrency data structure.
  * It allows us to combine and compose a group of memory operations and perform all of them in one single atomic operation.
  *
  * It supports ACI properties:
  * Atomicity - all updates either run once or not at all
  * Consistency - reads always get consistent view of the state, no partial updates
  * Isolation - multiple transactional updates are isolated and don't interfere with each other
  */

object STMDemo extends App {

  def withdraw(account: TRef[Long], amount: Long): STM[String, Unit] =
    account.get.flatMap { balance =>
      if (balance >= amount) account.update(_ - amount)
      else STM.fail("Insufficient funds")
    }

  def deposit(account: TRef[Long], amount: Long): STM[Nothing, Unit] =
    account.update(_ + amount)

  def transfer(from: TRef[Long], to: TRef[Long], amount: Long): zio.IO[String, Unit] =
    STM.atomically {
      for {
        _ <- deposit(to, amount)
        _ <- withdraw(from, amount)
      } yield ()
    }

  val program = for {
    alice <- TRef.makeCommit(100L)
    bob   <- TRef.makeCommit(0L)
    _     <- ZIO.collectAllPar(ZIO.replicate(10000)(transfer(alice, bob, 1).either))
    ab    <- alice.get.commit
    bb    <- bob.get.commit
    _     <- ZIO.succeed(println(s"Alice: $ab, Bob: $bb"))
  } yield ()

  Unsafe.unsafe { implicit unsafe => zio.Runtime.default.unsafe.run(program) }
}

/** Transactional data structures:
  *  - TRef
  *  - TArray
  *  - TSet
  *  - TSemaphore
  *    ...
  *
  * Benefits of STM:
  *
  * 1. Composable
  * 2. Declarative
  * 3. Optimistic Concurrency
  * 4. Lock-free
  * 5. Fine-grained locking
  *
  * Implications of STM:
  *
  * 1. Can't do effects inside STM
  * 2. Large allocations
  */

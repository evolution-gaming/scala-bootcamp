package com.evolutiongaming.bootcamp.effects

import java.time.Instant
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference

import cats.effect.concurrent._
import cats.effect.{Concurrent, ExitCode, IO, IOApp}
import cats.implicits.{catsSyntaxMonadErrorRethrow, catsSyntaxParallelSequence}
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.evolutiongaming.bootcamp.effects.IosCommon.logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

import scala.concurrent.duration.{DurationInt, FiniteDuration}

/*
 * In modern applications we tend to use some kind of a state one way or another.
 * We use counters, caches, locks, queues and so on.
 * But as we all have multicore processors in our machines we want to leverage the concurrency.
 * And here lies so many pitfalls.
 * The main problem is that in concurrent environment we determine the order of process execution without
 * providing additional synchronization / monitoring.
 * The best way to deal with concurrency is by using immutable data structures.
 * They will give you a superpower to pay less attention to how things will be executed.
 * But what if we have to store shared state across our application?
 * Java offers you multiple choices: synchronized blocks, volatile variables and Atomic* classes.
 * Let's try them!
 */

object SynchronizationCommon {

  trait Friends {
    def getSize: Int

    def put(s: String): Unit

    def getFriendsList: List[String]
  }

  def run(friends: Friends): Unit = {
    def myRunnable(name: String): Runnable = new Runnable {
      override def run(): Unit = {
        println("Thread " + Thread.currentThread().getName +
          s" friends size  ${friends.getSize}")
        println("Thread " + Thread.currentThread().getName +
          s" trying to add a friend $name")
        friends.put(name)
        println("Thread " + Thread.currentThread().getName +
          s" has friends size ${friends.getSize}")

        println("Thread " + Thread.currentThread().getName +
          s" has friends list ${friends.getFriendsList}")
      }
    }

    val pool = Executors.newFixedThreadPool(5)

    pool.submit(myRunnable("Henry"))
    pool.submit(myRunnable("Michael"))
    pool.submit(myRunnable("Bruce"))
    pool.submit(myRunnable("Robert"))

    pool.shutdown()
  }
}

/*
 * We can go with `AtomicReference`.
 *
 * `Atomic` prefix indicates that operations with this classes will be atomic.
 *
 * Java classes rely on Compare-and-Swap operation which is a machine-level operation
 * that insures that the swapping operation will be atomic.
 *
 * Are there any problems with this solution?
 */

object AtomicRefSyncExample {

  import SynchronizationCommon._

  class AtomicFriends extends Friends {
    private val friendsRef: AtomicReference[List[String]] = new AtomicReference(List.empty)

    override def getSize: Int = friendsRef.get().length

    override def put(s: String): Unit = friendsRef.updateAndGet { friendsList =>
      if (!friendsList.contains(s)) {
        friendsList ++ List(s)
      } else friendsList
    }

    override def getFriendsList: List[String] = friendsRef.get()
  }

  def main(args: Array[String]): Unit = {
    val friends = new AtomicFriends

    run(friends)

  }

}

object IosCommon {
  val logger = Slf4jLogger.getLogger[IO]
}

/*
 * But we are living in a beautiful world of FP,
 * so there should be something that is referentially transparent and composable, right?
 * If you will search something about how to store state in FP, then probably you will face State Monad.
 * Maybe we could just use State Monad ?
 * Turns out that State is no more than function S => (S,A) which is sequential by its definition.
 * So no concurrency for State.
 *
 * But we can build it by ourselves?
 *
 * Start with implementing IOAtomicRef.of(...) function
 * Then get and set
 * Then update and modify
 * Tip: can we implement update using modify ?
 *
 */
object ExerciseZero extends IOApp {

  import IosCommon.logger

  trait IOAtomicRef[A] {

    def get(): IO[A]

    def set(a: A): IO[Unit]

    def update(f: A => A): IO[Unit]

    def modify[B](f: A => (A, B)): IO[B]

  }

  class SimpleRef[A](ar: AtomicReference[A]) extends IOAtomicRef[A] {

    override def get(): IO[A] = ???

    override def set(a: A): IO[Unit] = ???

    override def update(f: A => A): IO[Unit] = ???

    override def modify[B](f: A => (A, B)): IO[B] = ???
  }

  /*
   * Question : Why we should wrap ref creation in effect ?
   */
  object IOAtomicRef {
    def of[A](a: A): IO[IOAtomicRef[A]] = ???
  }

  override def run(args: List[String]): IO[ExitCode] = {

    val getTest = for {
      ref <- IOAtomicRef.of(1)
      content <- ref.get()
      _ <- logger.info(s"get test content should be 1, content $content")
    } yield ()

    val setTest = for {
      ref <- IOAtomicRef.of(1)
      _ <- ref.set(42)
      content <- ref.get()
      _ <- logger.info(s"set test content should be 42, content $content")
    } yield ()

    val updateTest = for {
      ref <- IOAtomicRef.of(1)
      _ <- ref.update(_ + 1)
      _ <- ref.update(_ + 1)
      content <- ref.get()
      _ <- logger.info(s"update test content should be 3, content $content")
    } yield ()

    val modifyTest = for {
      ref <- IOAtomicRef.of(1)
      contentReturn <- ref.modify(current => (current + 20, current))
      contentState <- ref.get()
      _ <- logger.info(s"modify test contentReturn should be 1, contentReturn $contentReturn")
      _ <- logger.info(s"modify test contentState should be 21, contentState $contentState")
    } yield ()

    //    getTest *>
    //      setTest *>
    //      updateTest *>
    //      modifyTest *>
    IO(ExitCode.Success)
  }
}

/*
 * What is Ref ?
 * Purely functional mutable reference
 * Concurrent, lock-free
 * Always contains a value
 */

/*
 * Why do we need update ?
 * Because Ref#get and then Ref#set is not Atomic
 */
object GetSetExample extends IOApp {

  import IosCommon.logger

  def report(messagesRef: Ref[IO, List[String]], msg: String): IO[Unit] =
    for {
      t <- messagesRef.get
      _ <- logger.info(s"adding $msg to $t")
      _ <- messagesRef.set(msg :: t)
    } yield ()


  val program: IO[Unit] = for {
    messages <- Ref[IO].of(List.empty[String])
    _ <- List(report(messages, "one"), report(messages, "two")).parSequence.void
    msgs <- messages.get
    _ <- logger.info(s"messages after changes $msgs")
  } yield ()

  override def run(args: List[String]): IO[ExitCode] = program.as(ExitCode.Success)
}

/*
* Why do we need modify ?
* Because Ref#get could happen after another Ref#update. `update` and then `get` is not Atomic.
*/
object UpdateExample extends IOApp {

  import IosCommon.logger

  val counterRef: IO[Ref[IO, Int]] = Ref.of[IO, Int](0)

  def inc(counterRef: Ref[IO, Int]): IO[Unit] =
    for {
      _ <- counterRef.update(_ + 1)
      counter <- counterRef.get
      _ <- logger.info(s"counter value is $counter")
    } yield ()


  val program: IO[Unit] = for {
    counter <- Ref[IO].of(0)
    _ <- List(inc(counter), inc(counter)).parSequence.void
    v <- counter.get
    _ <- logger.info(s"counter after updates $v")
  } yield ()

  override def run(args: List[String]): IO[ExitCode] = program.as(ExitCode.Success)
}

/*
 * Ref#modify will allow you to perform update and return something in an atomic way.
*/
object ModifyExample extends IOApp {

  import IosCommon.logger

  def inc(ref: Ref[IO, Int]): IO[Unit] = ref.modify(s => (s + 1, s)).flatMap(i => logger.info(i.toString))

  val counterRef: IO[Ref[IO, Int]] = Ref.of[IO, Int](0)

  val program: IO[Unit] = for {
    counter <- counterRef
    _ <- List.fill(10)(inc(counter)).parSequence.void
    counterAfter <- counter.get
    _ <- logger.info(s"counter after update should be 10, $counterAfter")
  } yield ()

  override def run(args: List[String]): IO[ExitCode] = program.as(ExitCode.Success)
}

/*
 * Limitations of Ref is that we cannot have effectful updates on Ref
 * Try to think what will happen if we would try to implement following method ?
*/
object RefLimitation {
  import ExerciseZero.IOAtomicRef
  trait EffectfullRef[A] extends IOAtomicRef[A]{
    // ....
    def modifyM[B](f: A => IO[(A,B)]): IO[B]
  }

  class EffectfullRefImpl[A](ar: AtomicReference[A]) extends EffectfullRef[A] {

    override def get(): IO[A] = ???

    override def set(a: A): IO[Unit] = ???

    override def update(f: A => A): IO[Unit] = ???

    override def modify[B](f: A => (A, B)): IO[B] = ???

    override def modifyM[B](f: A => IO[(A, B)]): IO[B] = ???
  }
}

/*
 * Actually we can do this at some extent by introducing a functional locking mechanism.
 * Something like a Semaphore.
 */

/*
 * What is a Semaphore?
 * Purely functional semaphore implementation.
 * A semaphore has a non-negative number of permits available. Acquiring a permit
 * decrements the current number of permits and releasing a permit increases
 * the current number of permits. An acquire that occurs when there are no
 * permits available results in semantic blocking until a permit becomes available.
 *
 * Most used methods:
 * def available: F[Long] Returns the number of permits currently available. Always non-negative.
 * def acquire: F[Unit]  Acquires a single permit
 * def release: F[Unit]  Releases a single permit.
 * def withPermit[A](t: F[A]): F[A] Returns an effect that acquires a permit, runs the supplied effect, and then releases the permit.
 */
object SemaphoreExample extends IOApp {

  import IosCommon.logger

  def someExpensiveTask: IO[Unit] =
    IO.sleep(3.second) >>
      logger.info("expensive task took 3 second")

  def process1(sem: Semaphore[IO]): IO[Unit] =
    logger.info("start first process") >>
      sem.withPermit(someExpensiveTask) >>
      logger.info("finish fist process") >>
      process1(sem)


  def process2(sem: Semaphore[IO]): IO[Unit] =
    logger.info("start second process") >>
      sem.withPermit(someExpensiveTask) >>
      logger.info("finish second process") >>
      process2(sem)


  def run(args: List[String]): IO[ExitCode] =
    Semaphore[IO](1).flatMap { sem =>
      process1(sem).start.void *>
        process2(sem).start.void
    } *> IO.never.as(ExitCode.Success)
}

/*
 * Try to implement SerialRef which will semantically block on modify and wait until inner f is completed
 * Question: What will happen in case of a function `f` will never terminate inside update or modify?
 */
object SerialRefExercise extends IOApp {

  import IosCommon.logger

  trait SerialRef[F[_], A] {

    def get: F[A]

    def modify[B](f: A => F[(A, B)]): F[B]

    def update(f: A => F[A]): F[Unit]
  }

  def of[F[_] : Concurrent, A](value: A): F[SerialRef[F, A]] = {
    for {
      s <- Semaphore[F](1)
      r <- Ref[F].of(value)
    } yield {
      new SerialRef[F, A] {

        def get: F[A] = ???

        def modify[B](f: A => F[(A, B)]): F[B] = ???

        def update(f: A => F[A]): F[Unit] = ???
      }
    }
  }

  override def run(args: List[String]): IO[ExitCode] = {
    def modifyHelperIO(ref: SerialRef[IO, Int], duration: FiniteDuration, i: Int, s: String): IO[String] =
      logger.info(s"$s started") *> ref.modify(x => IO.sleep(duration) *> IO((x + i, s))).flatTap(s => logger.info(s"$s finished"))

    for {
      ref <- SerialRefExercise.of[IO, Int](1)
      _ <- List(modifyHelperIO(ref, 3.second, 10, "first modify"), modifyHelperIO(ref, 5.second, 20, "second modify")).parSequence.void
      value <- ref.get
      _ <- logger.info(s"ref value should be 31, $value")
    } yield (ExitCode.Success)
  }
}

/*
 * What is Deferred?
 * Purely functional synchronisation
 * Simple one-shot semantics
 * Semantic blocking
 * You can think of it as an functional version of a Promise
 * Deferred always starts empty
 * It can only be completed once and can never be modified or unset again.
 * `get` on a completed Deferred returns A.
 * `get` on an empty Deferred semantically blocks until a result is available.
 * `complete` on an empty Deferred puts a value in it and awakes the listeners.
 * `complete` on a full Deferred fails.
 *
 * Deferred is a cancelable data type, if the underlying F[_] is capable of it.
 * This means that cancelling a get will unsubscribe the registered listener and can thus avoid memory leaks.
 *
 * Common use case: ensure that processes will start in some order
 */
object DeferredExamples {
  val logger = Slf4jLogger.getLogger[IO]
}

object EnsureOrder extends IOApp {

  import DeferredExamples.logger

  def mainProcess(d: Deferred[IO, Unit]): IO[Unit] = {
    for {
      _ <- logger.info("starting main process")
      _ <- d.complete(())
      _ <- logger.info("completed main process")
    } yield ()

  }

  def dependantProcess(d: Deferred[IO, Unit]): IO[Unit] = {
    for {
      _ <- logger.info("starting dependant process")
      _ <- d.get
      _ <- logger.info("completed dependant process")
    } yield ()
  }

  override def run(args: List[String]): IO[ExitCode] = {
    val dEff = Deferred[IO, Unit]
    for {
      d <- dEff
      _ <- List(IO.sleep(3.seconds) *> mainProcess(d), dependantProcess(d)).parSequence
    } yield ExitCode.Success
  }
}

/*
 * What will happen if we fail somewhere before completing Deferred?
 * What can you do to avoid this?
 * Turns out that most basic technique is to just add a timeout on `Deferred#get()` with
 * `Deferred#get().timeout(...)`.
 * Or you can go with more interesting technique...
 */

object HandlingErrorsWithDeferred extends IOApp {

  import IosCommon.logger

  def maybeFail: IO[Long] = IO {
    val now = Instant.now.getEpochSecond
    if (now % 5 == 0)
      now
    else
      throw new IllegalStateException("BOOM!")
  }

  def mainProcess(d: Deferred[IO, Either[Throwable, Long]]): IO[Unit] = {
    for {
      _ <- logger.info("starting main process")
      t <- maybeFail.attempt
      _ <- logger.info(s"got $t in main process")
      _ <- d.complete(t)
      _ <- logger.info("completed main process")
    } yield ()

  }

  def dependantProcess(d: Deferred[IO, Either[Throwable, Long]]): IO[Unit] = {
    for {
      _ <- logger.info("starting dependant process")
      _ <- d.get.rethrow
      _ <- logger.info("completed dependant process")
    } yield ()
  }

  override def run(args: List[String]): IO[ExitCode] = {
    val dEff = Deferred[IO, Either[Throwable, Long]]
    for {
      d <- dEff
      _ <- List(mainProcess(d), dependantProcess(d)).parSequence.void
    } yield ExitCode.Success
  }.handleErrorWith(e => logger.error(s"got an error $e").as(ExitCode.Success))
}

/*
 * What about combining Refs and Deferred?
 * Implement a `memoize` function that takes some `f:F[A]` and memoizes it (stores the result of computation).
 * What will happen if the function `f` will fail with some error?
 */
object RefsExerciseTwo extends IOApp {

  def memoize[F[_], A](f: F[A])(implicit C: Concurrent[F]): F[F[A]] = ???

  override def run(args: List[String]): IO[ExitCode] = {

    val successProgram = IO {
      println("Hey!");
      42
    }

    /*
     * Should print
     * Hey!
     * 42
     * 42
     * */

    val successResult: IO[Unit] = for {
      mem <- memoize(successProgram)
      x <- mem
      _ <- IO(println(x))
      y <- mem
      _ <- IO(println(y))
    } yield ()


    val errorProgram = IO {
      println("Gonna Boom!");
      throw new IllegalArgumentException("BOOM")
    }

    /*
     * Should print
     * Gonna Boom!
     * java.lang.IllegalArgumentException: BOOM
     */

    val failedResult: IO[Unit] = (for {
      mem <- memoize(errorProgram)
      x <- mem
      _ <- IO(println(x))
      y <- mem
      _ <- IO(println(y))
    } yield ()).handleErrorWith(e => IO(println(e)))

    successResult *>
      failedResult *>
      IO(ExitCode.Success)
  }
}

/*
 * What is an MVar?
 * Comes from the Haskell world
 *
 * Use-cases:
 * As synchronized, thread-safe mutable variables
 * As channels, with take and put acting as “receive” and “send”
 * As a binary semaphore, with take and put acting as “acquire” and “release”
 *
 * Main methods:
 * def take: F[A] Empties the `MVar` if full, returning the contained value,
 * or blocks (asynchronously) until a value is available.
 *
 * def put(a: A): F[Unit] Fills the `MVar` if it is empty, or blocks (asynchronously)
 * if the `MVar` is full, until the given value is next in
 * line to be consumed on [[take]].
 */

object MVarQueueExample extends IOApp {

  import IosCommon.logger

  val N = 10
  val mvarF: IO[MVar2[IO, Int]] = MVar.empty[IO, Int]

  // Puts 'n', 'n+1', ..., 'N-1' to 'mvar'
  def produce(mvar: MVar2[IO, Int], n: Int): IO[Unit] = {
    logger.info(s"produce($n)") >> IO(if (n < N) {
      mvar.put(n).flatTap(_ => logger.info(s"produced $n")) >> produce(mvar, n + 1)
    } else {
      IO.unit
    }).flatten
  }

  // Takes 'N-c' values from 'mvar' and sums them. Fails if cannot take in 100 ms.
  def consume(mvar: MVar2[IO, Int], sum: Long, c: Int): IO[Long] = {

    logger.info(s"consume($sum, $c)") >> IO(if (c < N) {
      mvar.take
        .timeout(100.millisecond).flatTap(n => logger.info(s"consumed $n"))
        .flatMap { v =>
          consume(mvar, v + sum, c + 1)
        }
    } else {
      IO(sum)
    }).flatten
  }

  override def run(args: List[String]): IO[ExitCode] = for {
    mv <- mvarF
    _ <- consume(mv, 0, 0).start
    _ <- produce(mv, 0)
  } yield ExitCode.Success
}


/*
 * Question: what will happen in the code below ?
 */
object MVarBehavior extends IOApp {

  import IosCommon.logger

  override def run(args: List[String]): IO[ExitCode] = {
    val mvarF: IO[MVar2[IO, Int]] = MVar.of[IO, Int](1)
    for {
      mv <- mvarF
      i <- mv.take
      _ <- logger.info(s"got $i from MVar")
      i <- mv.take
      _ <- logger.info(s"now $i from MVar")
      _ <- logger.info(s"putting 10 to MVar")
      _ <- mv.put(10)
      i <- mv.take
      _ <- logger.info(s"and now got $i from MVar")
    } yield ExitCode.Success
  }
}

/*
 * Implement MySemaphore from one of previous exercises in terms of MVar
 *
 * Question: How we can adapt code if we want more to allow more than 1 number of permit ?
 */

object MySemaphoreMVarExercise extends IOApp {

  trait MySemaphore[F[_]] {
    def acquire: F[Unit]

    def release: F[Unit]

    def withPermit[A](fa: F[A]): F[A]
  }

  class MySemaphoreMVar[F[_] : Concurrent](mvar: MVar2[F, Unit]) extends MySemaphore[F] {
    override def acquire: F[Unit] = ???

    override def release: F[Unit] = ???

    override def withPermit[A](fa: F[A]): F[A] = ???
  }

  object MySemaphoreMVar {
    def of[F[_] : Concurrent]: F[MySemaphoreMVar[F]] = MVar.of[F, Unit](()).map(mv => new MySemaphoreMVar[F](mv))
  }


  def someExpensiveTask: IO[Unit] =
    IO.sleep(3.second) >>
      logger.info("expensive task took 3 second")

  def process1(sem: MySemaphore[IO]): IO[Unit] =
    logger.info("start first process") >>
      sem.withPermit(someExpensiveTask) >>
      logger.info("finish fist process") >>
      process1(sem)

  def process2(sem: MySemaphore[IO]): IO[Unit] =
    logger.info("start second process") >>
      sem.withPermit(someExpensiveTask) >>
      logger.info("finish second process") >>
      process2(sem)

  def run(args: List[String]): IO[ExitCode] = {
    for {
      mySem <- MySemaphoreMVar.of[IO]
      _ <- List(process1(mySem), process2(mySem)).parSequence
    } yield ExitCode.Success
  }
}


/*
 * Implement race method (who completed first - wins, other should be canceled) using MVar
 * Tip: Recall that we can use Fibers in order to schedule task in background
 */
object RaceMVarExercise extends IOApp {

  def race[A](taskA: IO[A], taskB: IO[A]): IO[A] = ???

  override def run(args: List[String]): IO[ExitCode] = {

    import IosCommon.logger

    def task(index: Int, sleepDuration: FiniteDuration): IO[Int] = {
      for {
        _ <- logger.info(s"$index is sleeping for $sleepDuration seconds")
        _ <- IO.sleep(sleepDuration)
      } yield index
    }

    for {
      index <- race(task(0, 3.seconds), task(1, 5.seconds))
      _ <- logger.info(s"index should be 0, $index ")
    } yield ExitCode.Success


  }
}







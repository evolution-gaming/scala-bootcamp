package com.evolutiongaming.bootcamp.effects

import java.time.Instant
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference

import cats.effect.concurrent._
import cats.effect.{Concurrent, ExitCode, IO, IOApp}
import cats.implicits.{catsSyntaxMonadErrorRethrow, catsSyntaxParallelSequence, catsSyntaxParallelTraverse}
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import cats.syntax.functor._
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

import scala.concurrent.duration.DurationInt

/*
 * In modern applications we tend to use some kind of a state one way or another.
 * We use counters, caches, locks, queues and so on.
 * But as we all have multicore processors in our machines we want to leverage the concurrency.
 * And here lies so many pitfalls.
 * The main problem is that in concurrent environment we determine the order of processes execution without
 * providing some additional synchronization/ monitoring.
 * The best way to deal with concurrency is by using immutable data structures.
 * They will gain you sort of superpower not to pay attention to how things will be executed.
 * But what if we have to store shared state across our app ?
 * Java offers you some of a choices : synchronized blocks, volatile variables and Atomic* classes.
 * Why should not try them ?
 *
 */

object SyncronizationCommon {

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
 * Example with no synchronization
 * Is there any problems with solution ?
 */
object NoSynchronization {

  import SyncronizationCommon._

  class SimpleFriends extends Friends {
    private var friendList: List[String] = List.empty

    def getSize: Int = friendList.size

    def put(s: String): Unit = {
      if (!friendList.contains(s)) {
        friendList = friendList ++ List(s)
      }
    }

    def getFriendsList: List[String] = friendList
  }

  def main(args: Array[String]): Unit = {
    val friends = new SimpleFriends
    run(friends)
  }


}

/*
 * We can use good-old synchronized from Java
 * Is there any problems with solution ?
 */
object SlightlyBetterSynchronization {

  import SyncronizationCommon._

  class SynchronizedFriends extends Friends {
    private var friendList: List[String] = List.empty

    def getSize: Int = friendList.length

    def put(s: String): Unit = this.synchronized {
      if (!friendList.contains(s)) {
        friendList = friendList ++ List(s)
      }
    }

    def getFriendsList: List[String] = friendList
  }

  def main(args: Array[String]): Unit = {

    val friends = new SynchronizedFriends

    run(friends)

  }
}

/*
 * We can go with AtomicReference
 * Atomic prefix indicates that operations with this classes will be atomic.
 * Java classes rely on Compare-and-swap operation which is a machine-level operation
 * that insures that swapping operation will be atomic.
 * Is there any problems with solution ?
 */

object AtomicRefSyncExample {

  import SyncronizationCommon._

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
 * so there should be something that is referentially transparent and composable, right ?
 * Or maybe we can build it by ourselves ?
 * But what about the State monad, it is used for storing state, right ?
 * Turns out that State is no more than function S => (S,A) which is sequential by its definition
 * So no concurrency for State
 */
object ExerciseZero extends IOApp {

  import IosCommon.logger

  trait IOAtomicRef[A] {

    def get(): IO[A]

    def set(a: A): IO[Unit]

    def update(f: A => A): IO[Unit]

    def modify[B](f: A => (A, B)): IO[B]

  }

  class SimpleRef[A] extends IOAtomicRef[A] {

    override def get(): IO[A] = ???

    override def set(a: A): IO[Unit] = ???

    override def update(f: A => A): IO[Unit] = ???

    override def modify[B](f: A => (A, B)): IO[B] = ???
  }

  object IOAtomicRef {
    def of[A](a: A): IO[IOAtomicRef[A]] = ???
  }

  override def run(args: List[String]): IO[ExitCode] = for {
    ref <- IOAtomicRef.of(1)
    _ <- ref.set(5)
    v <- ref.get()
    _ <- logger.info(s"value $v")
  } yield (ExitCode.Success)
}

/*
 * What is Ref ?
 * Purely functional mutable reference
 * Concurrent, lock-free
 * Always contains a value
 */

/*
*
* Ref#get and then Ref#set is not Atomic
*
*/
object GetSetExample extends IOApp {

  import IosCommon.logger

  def report(messagesRef: Ref[IO, List[String]], msg: String): IO[Unit] =
    for {
      t <- messagesRef.get
      _ <- logger.info(s"adding $msg to $t")
      _ <- messagesRef.set(msg :: t)
    } yield ()


  val program = for {
    messages <- Ref[IO].of(List.empty[String])
    _ <- List(report(messages, "one"), report(messages, "two")).parSequence.void
    msgs <- messages.get
    _ <- logger.info(s"messages after changes $msgs")
  } yield ()

  override def run(args: List[String]): IO[ExitCode] = program.as(ExitCode.Success)
}

/*
*
* Ref#get could happens after another Ref#update. Update and then Get is not Atomic
*
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


  val program = for {
    counter <- Ref[IO].of(0)
    _ <- List(inc(counter), inc(counter)).parSequence.void
    v <- counter.get
    _ <- logger.info(s"counter after updates $v")
  } yield ()

  override def run(args: List[String]): IO[ExitCode] = program.as(ExitCode.Success)
}

/*
*
* Ref#modify will allow you to perform update and return something in atomic way
*
*/
object ModifyExample extends IOApp {

  import IosCommon.logger

  def inc(ref: Ref[IO, Int]): IO[Unit] = ref.modify(i => i + 1 -> i)

  val counterRef = Ref.of[IO, Int](0)

  val program = for {
    counter <- counterRef
    _ <- List.fill(10)(inc(counter)).parTraverse(_.start).void
    counterAfter <- counter.get
    _ <- logger.info(s"counter after update $counterAfter")
  } yield ()

  override def run(args: List[String]): IO[ExitCode] = program.as(ExitCode.Success)
}

/*
* Limitations of Ref is that we cannot have efectfull updates on Ref
* Try to think what will happen if we would try to implement following method ?
*
*/
object RefLimitation {

  trait Ref[A] {
    // ....
    def updateM(f: A => IO[A]): IO[Unit]
  }

}

/*
 * Actually we can do this at some extent by introducing some functional locking mechanism
 * Something like Semaphore
 * What is Semaphore ?
 * Purely functional semaphore realization.
 * A semaphore has a non-negative number of permits available. Acquiring a permit
 * decrements the current number of permits and releasing a permit increases
 * the current number of permits. An acquire that occurs when there are no
 * permits available results in semantic blocking until a permit becomes available.
 *
 */
object SemaphoreExample extends IOApp {
  def putStrLn[A](a: A): IO[Unit] = IO(println(a))

  def someExpensiveTask: IO[Unit] =
    IO.sleep(2.second) >>
      putStrLn("expensive task") >>
      someExpensiveTask

  def process1(sem: Semaphore[IO]): IO[Unit] =
    sem.withPermit(someExpensiveTask) >> process1(sem)

  def process2(sem: Semaphore[IO]): IO[Unit] =
    sem.withPermit(someExpensiveTask) >> process2(sem)

  def run(args: List[String]): IO[ExitCode] =
    Semaphore[IO](1).flatMap { sem =>
      process1(sem).start.void *>
        process2(sem).start.void
    } *> IO.never.as(ExitCode.Success)
}

/*
 * What will happen in case of f will never terminate inside update ?
 */
object SerialRef {

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

        def get: F[A] = r.get

        def modify[B](f: A => F[(A, B)]): F[B] = {
          s.withPermit {
            for {
              a <- r.get
              ab <- f(a)
              (a, b) = ab
              _ <- r.set(a)
            } yield b
          }
        }

        def update(f: A => F[A]): F[Unit] = {
          modify { a =>
            for {
              a <- f(a)
            } yield {
              (a, ())
            }
          }
        }
      }
    }
  }
}

/*
 * What is Deferred ?
 * Purely functional synchronisation
 * Simple one-shot semantics
 * Semantic blocking
 * You can think of it as an functional version of Promise
 * Deferred always starts empty
 * It can only be completed once and can never be modified or unset again.
 * get on a completed Deferred returns A,
 * get on an empty Deferred semantically blocks until a result is available.
 * complete on an empty Deferred puts a value in it and awakes the listeners.
 * complete on a full Deferred fails.
 *
 * Common use case: ensure that processes will start in some order
* */
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
 * What will happen if we fail somewhere before completing Deferred ?
 * What can you avoid this ?
 * Turns out that most basic technique is to just add a timeout on Deferred#get() like so Deferred#get().timeout(...)
 * Or you can go with more interesting technique
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
 * What about combining Refs and Deferred ?
 * Implement a memoize function that takes some `f:F[A]` and memoizes it (stores the result of computation)
 * What will happen if `f` will fail with some error ?
 */
object RefsExerciseTwo extends IOApp {

  def memoize[F[_], A](f: F[A])(implicit C: Concurrent[F]): F[F[A]] = {
    Ref.of[F, Option[Deferred[F, Either[Throwable, A]]]](None).map { ref =>
      Deferred[F, Either[Throwable, A]].flatMap { d =>
        ref.modify {
          case None => Some(d) -> f.attempt.flatTap(d.complete)
          case v@Some(other) => v -> other.get
        }
          .flatten
          .rethrow
      }

    }
  }

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

    val sucessResult: IO[Unit] = for {
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
     * */

    val failedResult: IO[Unit] = (for {
      mem <- memoize(errorProgram)
      x <- mem
      _ <- IO(println(x))
      y <- mem
      _ <- IO(println(y))
    } yield ()).handleErrorWith(e => IO(println(e)))

    sucessResult *> failedResult *> IO(ExitCode.Success)
  }
}

/*
 * Can we do it with Semaphore ?
 */
object SemaphoreAttempt extends IOApp {

  trait MySemaphore[F[_]] {
    def acquire: F[Unit]

    def release: F[Unit]

    def withPermit[A](t: F[A]): F[A]
  }

  class MySemaphoreImpl[F[_] : Concurrent] extends MySemaphore[F] {

    override def acquire: F[Unit] = ???

    override def withPermit[A](t: F[A]): F[A] = ???

    override def release: F[Unit] = ???
  }

  override def run(args: List[String]): IO[ExitCode] = ???
}

/*
 * What is MVar?
 * Came from Haskell world
 * Use-cases:
 * As synchronized, thread-safe mutable variables
 * As channels, with take and put acting as “receive” and “send”
 * As a binary semaphore, with take and put acting as “acquire” and “release”
 */

object MvarQueueExample extends IOApp {

  import IosCommon.logger

  val N = 10
  val mvarF: IO[MVar2[IO, Int]] = MVar.empty[IO, Int]

  //Puts 'n', 'n+1', ..., 'N-1' to 'mvar'
  def produce(mvar: MVar2[IO, Int], n: Int): IO[Unit] = {
    logger.info(s"produce($n)") >> IO(if (n < N) {
      mvar.put(n).flatTap(_ => logger.info(s"produced $n")) >> produce(mvar, n + 1)
    } else {
      IO.unit
    }).flatten
  }

  //Takes 'N-c' values from 'mvar' and sums them. Fails if cannot take in 100 ms.
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
 * Question: what will happen in code below ?
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

object SharedState {
  // TODO: https://typelevel.org/cats-effect/concurrency/basics.html
  // TODO: https://typelevel.org/cats-effect/concurrency/deferred.html
  // TODO: https://typelevel.org/cats-effect/concurrency/mvar.html
  // TODO: https://typelevel.org/cats-effect/concurrency/ref.html
  // TODO: https://typelevel.org/cats-effect/concurrency/semaphore.html
  // TODO: Exercises
  // TODO: Homework
}







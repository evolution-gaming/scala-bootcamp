package com.evolutiongaming.bootcamp.effects

import cats.effect.{Concurrent, ExitCode, IO, IOApp, Timer}
import cats.implicits._

import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.concurrent.{Future, blocking}

/*
 * Why cancellable IO is needed? Why it's better then Future?
 * - One cannot just simply cancel Future
 *   - cannot cancel on error condition
 *   - cannot cancel on race
 *
 * Futures will continue doing their work, spending precious computing resources, till the work is done
 *
 * Cancellable IO to the rescue!
 */
object BasicCancellableIO extends IOApp {

  import scala.concurrent.ExecutionContext.Implicits.global

  // dumb example on future timeouts via race
  // NEVER! use such implementation in real code
  val futureTimeout = {
    def runTask(i: Int): Future[Unit] = Future {
      blocking {
        (1 to i).foreach { iteration =>
          println(s"${Thread.currentThread().toString} Starting iteration:$iteration work")
          Thread.sleep(1000)
          println(s"${Thread.currentThread().toString} Done iteration:$iteration working")
        }
      }
    }

    def completeAfter(timeout: FiniteDuration): Future[Unit] = Future {
      blocking {
        Thread.sleep(timeout.toMillis)
        println(s"${Thread.currentThread().toString} Completing future after ${timeout} ")
      }
    }

    IO.fromFuture(IO.delay(Future.firstCompletedOf[Unit](Seq(runTask(10), completeAfter(5.seconds))))) // *> IO.sleep(5.seconds)
  }

  val ioTimeout = {
    def runTask(i: Int): IO[Unit] = (1 to i).toList.map { iteration =>
      for {
        _ <- IO.delay(println(s"${Thread.currentThread().toString} Starting iteration:$iteration work"))
        _ <- IO.sleep(1.second)
        _ <- IO.delay(println(s"${Thread.currentThread().toString} Done iteration:$iteration working"))
      } yield ()
    }.sequence.void

    runTask(10).timeout(5.seconds).attempt *> IO.delay(println(s"${Thread.currentThread().toString} Cancelled")) *> IO.sleep(5.seconds)
  }

  val raceAndCancel = {
    val tick = (IO.delay(println("Working work long long never terminating")) *> IO.sleep(1.second)).foreverM.void
    val workThatDoesFaster = IO.sleep(5.seconds) *> IO.delay(println("Work has been done"))
    // show example of
    // IO.race() and how one side is cancelled
    IO.race(tick, workThatDoesFaster).void *> IO.sleep(5.seconds) *> IO.delay(println("Terminating"))
  }

  val exerciseSelfMadeIoTimeout = {
    val tick = (IO.delay(println("Working work long long never terminating")) *> IO.sleep(1.second)).foreverM.void

    //alternative to above abstracting from effect type using type classes
    def tickF[F[_]](implicit F: Concurrent[F]): F[Unit] = F.raiseError(???)

    def timeoutIO[A](task: IO[A], timeout: FiniteDuration): IO[A] = IO.raiseError(???)

    def timeoutF[F[_], A](task: F[A], timeout: FiniteDuration)(implicit F: Concurrent[F], T: Timer[F]): F[A] = F.raiseError(???)

    IO.never
  }

  override def run(args: List[String]): IO[ExitCode] = for {
    _ <- futureTimeout
    //    _ <- ioTimeout
    //    _ <- raceAndCancel
    //    _ <- exerciseSelfMadeIoTimeout
  } yield ExitCode.Success
}

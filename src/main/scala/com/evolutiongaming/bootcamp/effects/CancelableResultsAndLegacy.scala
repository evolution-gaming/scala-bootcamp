package com.evolutiongaming.bootcamp.effects

import java.util.concurrent.atomic.AtomicBoolean

import cats.effect.{ExitCode, IO, IOApp}
import com.evolutiongaming.bootcamp.effects.Console.Real.putStrLn

import scala.annotation.tailrec
import scala.concurrent.duration._

/* Cancellation on legacy code,
 * When writing your cancellable code, be aware that cancellation is a concurrent action.
 * That is, there is no synchronization provided by Cats Effect IO for it.
 * Therefore, if your effect code is doing an operation that isn't safe to do concurrently with cancellation,
 * it can lead data corruption or other errors.
 * You can solve it, for example, by introducing a lock, as per Cats Effect IO documentation here:
 * https://typelevel.org/cats-effect/datatypes/io.html#gotcha-cancellation-is-a-concurrent-action
 */
object CancelableResultsAndLegacy extends IOApp {

  private val cancelableLegacyIntegrationProgram = {
    class UglyLegacyCode {
      private val cancelled = new AtomicBoolean(false)

      @tailrec
      private def longRecursiveCompute(x: Long, until: Long): Long = {
        if (cancelled.get()) {
          throw new InterruptedException("compute interrupted")
        } else if (x >= until) {
          x
        } else {
          println(s" ${Thread.currentThread().toString} Calculating in longRecursiveCompute: $x")
          Thread.sleep(1000)
          longRecursiveCompute(x + x, until)
        }
      }

      def compute(i: Long, until: Long)(onComplete: Long => Unit, onError: Exception => Unit): Unit = {
        val t = new Thread(() => {
          try {
            onComplete(longRecursiveCompute(i, until))
          } catch {
            case e: InterruptedException => onError(e)
          }
        })
        t.start()
      }

      def cancel(): Unit = cancelled.set(true)
    }

    for {
      _ <- putStrLn("Launching cancelable")
      io = IO.cancelable[Long] { cb =>
        val legacy = new UglyLegacyCode
        legacy.compute(2L, Long.MaxValue)(res => cb(Right(res)), e => cb(Left(e)))
        IO.delay(legacy.cancel())
      }
      fiber <- io.start
      _ <- putStrLn(s"Started $fiber")
      res <- IO.race(IO.sleep(10.seconds), fiber.join)
      _ <-
        res.fold(
          _ => putStrLn(s"cancelling $fiber...") *> fiber.cancel *> putStrLn("IO cancelled"),
          i => putStrLn(s"IO completed with: $i")
        )
    } yield ()
  }
  override def run(args: List[String]): IO[ExitCode] = for {
    _ <- cancelableLegacyIntegrationProgram
  } yield ExitCode.Success
}

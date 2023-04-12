package com.evolutiongaming.bootcamp.streaming.homework

import cats.effect.{IO, Ref}
import cats.data.{NonEmptyList => Nel}
import cats.effect.kernel.Clock
import cats.implicits._
import com.evolutiongaming.bootcamp.streaming.homework.StreamingHomeWork.GameEngine._
import org.scalatest.wordspec.AnyWordSpec
import com.evolutiongaming.bootcamp.streaming.homework.StreamingHomeWork._
import fs2._
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration.{Duration, DurationLong, FiniteDuration}
import cats.effect.unsafe.implicits.global

import java.time.Instant

class StreamingHomeWorkSpec extends AnyWordSpec with Matchers {
  "StreamingHomeWork" when {
    "Exercise 1" should {
      "met all guarantees and provide desired throughput" in {
        val commandsCount = 10
        val commands      = List // TODO use iterate
          .unfold(1) { current => if (current == commandsCount + 1) None else Some(current, current + 1) }
          .map(Command(_, Durations(load = 500.millis, validate = 5.millis, effect = 50.millis)))

        def appendLatency(eventsSize: Int): Double = math.pow(eventsSize, 0.5) * 100

        val program = for {
          (commandHandlingFlow, effectsCollector) <- flowToTest(1, 1, 1, 1, appendLatency, cmdHandlingFlow1)
          _                                       <- IO.delay { println(s"${Instant.now} stream started") }
          (elapsed, _) <- Stream.emits(commands).through(commandHandlingFlow).compile.drain.timed
          elapsedMillis = elapsed.toMillis
          elapsedStr    = s"${elapsedMillis / 1000} seconds, ${elapsedMillis - 1000 * (elapsedMillis / 1000)} millis"
          _            <- IO.delay { println(s"${Instant.now} stream finished, elapsed: $elapsedStr") }
          commandsCollected <- effectsCollector.get
        } yield {
          commandsCollected.reverse shouldBe commands
          // 500 millis here is a time to start a stream and execute intermediate code,
          // while `655 * commandsCount` is a theoretical duration
          elapsed - (655 * commandsCount).millis shouldBe <(500.millis)
        }

        program.unsafeRunSync()
      }
    }

    "Exercise 2" should {
      "met all guarantees and provide desired throughput" in {
        val commandsCount = 100
        val commands      = List
          .unfold(1) { current => if (current == commandsCount + 1) None else Some(current, current + 1) }
          .map(Command(_, Durations(load = 500.millis, validate = 5.millis, effect = 50.millis)))

        def appendLatency(eventsSize: Int): Double = math.pow(eventsSize, 0.5) * 100

        val program = for {
          (commandHandlingFlow, effectsCollector) <- flowToTest(5, 1, 1, 1, appendLatency, cmdHandlingFlow2)
          _                                       <- IO.delay {
            println(s"${Instant.now} stream started")
          }
          (elapsed, _) <- Stream.emits(commands).through(commandHandlingFlow).compile.drain.timed
          elapsedMillis = elapsed.toMillis
          elapsedStr    = s"${elapsedMillis / 1000} seconds, ${elapsedMillis - 1000 * (elapsedMillis / 1000)} millis"
          _            <- IO.delay {
            println(s"${Instant.now} stream finished, elapsed: $elapsedStr")
          }
          commandsCollected <- effectsCollector.get
        } yield {
          commandsCollected.reverse shouldBe commands
          // 1500 millis here is a time to start a stream and execute intermediate code,
          // while `100 * 155 + 500` is a theoretical duration
          elapsed - (100 * 155 + 500).millis shouldBe <(1500.millis)
        }

        program.unsafeRunSync()
      }
    }

    "Exercise 3" should {
      "met all guarantees and provide desired throughput" in {
        val commandsCount = 100
        val commands = List
          .unfold(1) { current => if (current == commandsCount + 1) None else Some(current, current + 1) }
          .map(Command(_, Durations(load = 500.millis, validate = 5.millis, effect = 50.millis)))

        def appendLatency(eventsSize: Int): Double = math.pow(eventsSize, 0.5) * 100

        val program = for {
          (commandHandlingFlow, effectsCollector) <- flowToTest(9, 1, 1, 8, appendLatency, cmdHandlingFlow3)
          _ <- IO.delay {
            println(s"${Instant.now} stream started")
          }
          (elapsed, _) <- Stream.emits(commands).through(commandHandlingFlow).compile.drain.timed
          elapsedMillis = elapsed.toMillis
          elapsedStr = s"${elapsedMillis / 1000} seconds, ${elapsedMillis - 1000 * (elapsedMillis / 1000)} millis"
          _ <- IO.delay {
            println(s"${Instant.now} stream finished, elapsed: $elapsedStr")
          }
          commandsCollected <- effectsCollector.get
        } yield {
          commandsCollected.reverse shouldBe commands
          // 1500 millis here is a time to start a stream and execute intermediate code,
          // while `100 * ~90 + 500` is a theoretical duration
          elapsed - (100 * 90 + 500).millis shouldBe <(1000.millis)
        }

        program.unsafeRunSync()
      }
    }

    "Exercise 4" should {
      "met all guarantees and provide desired throughput" in {
        val commandsCount = 100
        val commands = List
          .unfold(1) { current => if (current == commandsCount + 1) None else Some(current, current + 1) }
          .map(Command(_, Durations(load = 500.millis, validate = 5.millis, effect = 50.millis)))

        def appendLatency(eventsSize: Int): Double = math.pow(eventsSize, 0.5) * 100

        val program = for {
          (commandHandlingFlow, effectsCollector) <- flowToTest(21, 1, 1, 8, appendLatency, cmdHandlingFlow4)
          _ <- IO.delay {
            println(s"${Instant.now} stream started")
          }
          (elapsed, _) <- Stream.emits(commands).through(commandHandlingFlow).compile.drain.timed
          elapsedMillis = elapsed.toMillis
          elapsedStr = s"${elapsedMillis / 1000} seconds, ${elapsedMillis - 1000 * (elapsedMillis / 1000)} millis"
          _ <- IO.delay {
            println(s"${Instant.now} stream finished, elapsed: $elapsedStr")
          }
          commandsCollected <- effectsCollector.get
        } yield {
          commandsCollected.reverse shouldBe commands
          // 1500 millis here is a time to start a stream and execute intermediate code,
          // while `6500.millis` is an empirical duration
          elapsed - 6500.millis shouldBe <(300.millis)
        }

        program.unsafeRunSync()
      }
    }
  }

  final case class Durations(load: FiniteDuration, validate: FiniteDuration, effect: FiniteDuration)
  object Durations {
    val Zero = Durations(Duration.Zero, Duration.Zero, Duration.Zero)
  }

  final case class State(value: Int)
  final case class Command(value: Int, durations: Durations)
  final case class Event(value: Int)

  /* maxAppendSize is not about max events size, but about max events batch size, produced by one command,
   * but since we produce one event per command, it's okay to compare with `events.size` here
   * TODO consider to refactor this ^
   */
  def flowToTest(
    maxLoadDiff: Int,
    maxValidateDiff: Int,
    maxEffectDiff: Int,
    maxAppendSize: Int,
    appendLatency: Int => Double,
    flowOf: (GameEngine[IO, State, Command, Event], Append[IO, Event], State) => Pipe[IO, Command, Unit],
  ): IO[(Pipe[IO, Command, Unit], Ref[IO, List[Command]])] =
    for {
      engineWithCollectors <- gameEngineWithGuarantees(maxLoadDiff, maxValidateDiff, maxEffectDiff)
      (gameEngine, effectsCollectorRef, appendsCollectorRef) = engineWithCollectors
      append                                                 = new Append[IO, Event] {
        def apply(events: Nel[Event]): IO[Unit] = if (events.size > maxAppendSize)
          IO.raiseError(
            new IllegalStateException(
              s"max append size guarantee is not met, maxAppendSize: $maxAppendSize, events: $events"
            )
          )
        else {
          for {
            _                <- printTimed(s"append started, events: $events")
            appendsCollector <- appendsCollectorRef.get
            _                <-
              if (appendsCollector.headOption.exists(_.last.value != events.head.value - 1))
                IO.raiseError(
                  new IllegalStateException(
                    s"sequential append guarantee is not met, appendsCollector.headOption: ${appendsCollector.headOption}, events: $events"
                  )
                )
              else IO.sleep(appendLatency(events.size).round.millis)
            _                <- appendsCollectorRef.update(events :: _)
            _                <- printTimed(s"append finished, events: $events")
          } yield ()
        }
      }
      initialState                                           = State(0)
    } yield flowOf(gameEngine, append, initialState) -> effectsCollectorRef

  def gameEngineWithGuarantees(
    maxLoadDiff: Int,
    maxValidateDiff: Int,
    maxEffectDiff: Int,
  ): IO[(GameEngine[IO, State, Command, Event], Ref[IO, List[Command]], Ref[IO, List[Nel[Event]]])] = {
    for {
      effectsCollectorRef <- Ref.of[IO, List[Command]](List.empty) // TODO consider using Nel.one[Command](0)
      appendsCollectorRef <- Ref.of[IO, List[Nel[Event]]](List.empty)
    } yield (
      new GameEngine[IO, State, Command, Event] {
        def load(cmd: Command, stale: State): IO[Validate] = {
          if (cmd.value - stale.value > maxLoadDiff)
            IO.raiseError(
              new IllegalStateException(
                s"max load diff guarantee is not met, maxLoadDiff: $maxLoadDiff, cmd: $cmd, stale: $stale"
              )
            )
          else {
            for {
              _ <- printTimed(s"load started, cmd: $cmd")
              _ <- IO.sleep(cmd.durations.load)
              _ <- printTimed(s"load finished, cmd: $cmd")
            } yield { state =>
              if (cmd.value - state.value > maxValidateDiff)
                IO.raiseError(
                  new IllegalStateException(
                    s"max validate diff guarantee is not met, maxValidateDiff: $maxValidateDiff, cmd: $cmd, state: $state"
                  )
                )
              else {
                for {
                  _ <- printTimed(s"validate started, cmd: $cmd")
                  _ <- IO.sleep(cmd.durations.validate)
                  _ <- printTimed(s"validate finished, cmd: $cmd")
                } yield {
                  val effect = for {
                    _                <- printTimed(s"effect started, cmd: $cmd")
                    appendsCollector <- appendsCollectorRef.get
                    effectsCollector <- effectsCollectorRef.get
                    _                <-
                      if (
                        effectsCollector.headOption.exists(lastEffect => cmd.value - lastEffect.value > maxEffectDiff)
                      )
                        IO.raiseError(
                          new IllegalStateException(
                            s"max effect diff guarantee is not met, maxEffectDiff: $maxEffectDiff, cmd: $cmd, lastEffect: ${effectsCollector.headOption}"
                          )
                        )
                      else if (!appendsCollector.headOption.exists(_.last.value >= cmd.value))
                        IO.raiseError(
                          new IllegalStateException(
                            s"effect after append guarantee is not met, appendsCollector.headOption: ${appendsCollector.headOption}, cmd: $cmd"
                          )
                        )
                      else IO.sleep(cmd.durations.effect)
                    _                <- effectsCollectorRef.update(cmd :: _)
                    _                <- printTimed(s"effect finished, cmd: $cmd")
                  } yield ()
                  CmdDirective(Change(State(cmd.value), Nel.one(Event(cmd.value))).some, effect).asRight
                }
              }
            }: Validate
          }
        }
      },
      effectsCollectorRef,
      appendsCollectorRef,
    )
  }

  def printTimed(msg: String): IO[Unit] = IO { println(s"${Instant.now} $msg") }
}

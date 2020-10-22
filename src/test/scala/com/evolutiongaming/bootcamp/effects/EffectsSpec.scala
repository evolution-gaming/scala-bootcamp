package com.evolutiongaming.bootcamp.effects

import cats.effect.{ExitCode, IO}
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.Assertion
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers

class EffectsSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {
  final class TestConsole(var providedInput: List[String] = Nil, var expectedOutput: List[String] = Nil) extends Console {
    def expectOutput(x: String): TestConsole = {
      expectedOutput = expectedOutput :+ x
      this
    }

    def provideInput(x: String): TestConsole = {
      providedInput = providedInput :+ x
      this
    }

    def expectEmpty: IO[Unit] =
      if (providedInput.isEmpty && expectedOutput.isEmpty) IO.unit
      else IO.raiseError(sys.error(s"Non empty console: $providedInput, $expectedOutput"))

    override def putStrLn(value: String): IO[Unit] = expectedOutput match {
      case Nil =>
        IO.raiseError(sys.error(s"Writing `$value` unexpectedly"))

      case x :: xs if x == value =>
        expectedOutput = xs
        IO.unit

      case x :: xs =>
        IO.raiseError(sys.error(s"Writing `$value` but expected `$x` (remaining in buffer $xs)"))
    }

    override def readStrLn: IO[String] = providedInput match {
      case Nil =>
        IO.raiseError(sys.error(s"Cannot read - out of provided input"))

      case x :: xs =>
        providedInput = xs
        IO.pure(x)
    }
  }

  import Exercise1_Functional._

  private def check(f: TestConsole => TestConsole, exitCode: ExitCode): IO[Assertion] = {
    val console = f(new TestConsole)

    val program = for {
      result  <- process(console)
      _       <- console.expectEmpty
    } yield result

    program.asserting(_ shouldBe exitCode)
  }

  "Exercise 1" - {
    "dogs" in {
      check(
        _
          .expectOutput("What is your favourite animal?")
          .provideInput("dogs")
          .expectOutput("Be the person your dog thinks you are."),
        ExitCode.Success,
      )
    }

    "cats" in {
      check(
        _
          .expectOutput("What is your favourite animal?")
          .provideInput("cats")
          .expectOutput("In ancient times cats were worshipped as gods; they have not forgotten this."),
        ExitCode.Success,
      )
    }

    "some wrong answers" in {
      check(
        _
          .expectOutput("What is your favourite animal?")
          .provideInput("")
          .expectOutput("Empty input is not valid, try again...")
          .expectOutput("What is your favourite animal?")
          .provideInput(" ")
          .expectOutput("Empty input is not valid, try again...")
          .expectOutput("What is your favourite animal?")
          .provideInput("elephants")
          .expectOutput("I don't know what to say about 'elephants'."),

        ExitCode.Success,
      )
    }

    "too many wrong answers" in {
      check(
        _
          .expectOutput("What is your favourite animal?")
          .provideInput("")
          .expectOutput("Empty input is not valid, try again...")
          .expectOutput("What is your favourite animal?")
          .provideInput(" ")
          .expectOutput("Empty input is not valid, try again...")
          .expectOutput("What is your favourite animal?")
          .provideInput("   ")
          .expectOutput("I am disappoint. You have failed to answer too many times."),

        ExitCode.Error,
      )
    }
  }
}

package com.evolutiongaming.bootcamp.cats_effects

import java.util.concurrent.Executors

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import scala.concurrent.ExecutionContext
import scala.io.Source

object EffectExercises extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    object Exercise_1 {
      def readResourceFile(
                            fileName: String
                          ): String =
        Source
          .fromResource(fileName)
          .mkString

      var counter = 0
      def increment: Int = {
        counter += 1
        counter
      }

      def buildValue(value: Int): Int =
        value + counter
    }

    object Exercise_2 {
      val treasure = "Treasure".some.some.some
      def showMeYourPower(entity: Option[Option[Option[String]]]): String = ???

      println(showMeYourPower(treasure))
    }

    object Exercise_3 {
      def putStrlLn(value: String): IO[Unit] = ???
      def readLn: IO[String] = ???
    }

    object Exercise_4 {
      def BOOM: IO[Unit] = IO.raiseError(new RuntimeException("I'm DarkSide, Try to stop me!!!"))
      def cameSuperManAndSaveUs[S, R](entity: IO[S]): IO[R] = ???
      case class Citizen(name: String, thankYouPhrase: String)
      def getCivilians: IO[List[Citizen]] = ???

      for {
        civilians <- getCivilians
        _ <- cameSuperManAndSaveUs[Unit, Unit](BOOM)
        _ <- IO(
          civilians
            .foreach(
              citizen =>
                println(citizen.thankYouPhrase)
            )
        )
      } yield ()
    }

    object Exercise_5 {
      def whileInThread(executionContext: ExecutionContext): Unit =
        executionContext.execute(() => {
          while (true) {
            println("Maybe they wanna kill us!!!!")
            Thread.sleep(1000)
          }
        })

      // IO.cancelable()
      val executionContext: ExecutionContext = ???
      def calmDownMan(executionContext: ExecutionContext): IO[Unit] = ???

      for {
        fiber <- calmDownMan(executionContext).start
        _ <- fiber.cancel
      } yield ()
    }

    object Exercise_6 {
      val fixedThreadPool = IO.contextShift(ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(1)))
      def fib(n: Int, a: Long = 0, b: Long = 1): IO[Long] =
        IO.suspend {
          if (n > 0) fib(n - 1, b, a + b)
          else IO.pure(a)
        }

      for {
        fiber1 <- fib(300).start(fixedThreadPool)
        fiber2 <- fib(300).start(fixedThreadPool)
        _ <- fiber1.join
        _ <- fiber2.join
      } yield ()
    }

    IO.pure(ExitCode.Success)
  }
}

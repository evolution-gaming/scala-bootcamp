package com.evolutiongaming.bootcamp.effects

import cats.effect.{ExitCode, IO, IOApp, Resource}
import cats.effect.implicits._
import cats.implicits._

import java.io.{Closeable, PrintWriter}
import scala.io.{BufferedSource, Source}

// Revision:

// delay vs pure
// TF is like imports
// raise error is like throw an exception

object CombiningFiles extends App {
  println("Started")

  val input1 = Source.fromFile("file1.txt")
  val input2 = Source.fromFile("file2.txt")
  val output = new PrintWriter("file3.txt")

  input1.getLines().foreach(output.println)
  input2.getLines().foreach(output.println)

  input1.close()
  input2.close()
  output.close()

  println("Finished")
}

object CombiningFilesIO extends IOApp {
  def openInput(filename: String): IO[BufferedSource] =
    IO.blocking(Source.fromFile(filename))

  def openOutput(filename: String): IO[PrintWriter] =
    IO.blocking(new PrintWriter(filename))

  def readAll(input: BufferedSource): IO[List[String]] =
    IO.blocking(input.getLines().toList)

  def printLine(output: PrintWriter, string: String): IO[Unit] =
    IO.blocking(output.println(string))

  def close(closable: Closeable): IO[Unit] =
    IO.blocking(closable.close())

  val program = for {
    _ <- IO.delay(println("Started"))

    // TODO: combine files (see CombiningFiles)

    _ <- IO.delay(println("Finished"))
  } yield ()

  override def run(args: List[String]): IO[ExitCode] = program.as(ExitCode.Success)
}

object CombiningFilesResource extends IOApp {
  def openInput(filename: String): Resource[IO, BufferedSource] =
    Resource.make(IO.blocking(Source.fromFile(filename)))(r => IO.blocking(r.close()))

  def openOutput(filename: String): Resource[IO, PrintWriter] =
    Resource.make(IO.blocking(new PrintWriter(filename)))(r => IO.blocking(r.close()))

  def readAll(input: BufferedSource): IO[List[String]] =
    IO.blocking(input.getLines().toList)

  def printLine(output: PrintWriter, string: String): IO[Unit] =
    IO.blocking(output.println(string))

  val files: Resource[IO, (BufferedSource, BufferedSource, PrintWriter)] = for {
    input1 <- openInput("file1.txt")
    input2 <- openInput("file2.txt")
    output <- openOutput("file3.txt")
  } yield (input1, input2, output)

  // different monads in for
  val program: IO[Unit] = {
    for {
      _ <- IO.delay(println("Started"))

      _ <- files.use { case (input1, input2, output) =>
        ??? // TODO: combine files (see CombiningFiles)
      }

      _ <- IO.delay(println("Finished"))
    } yield ()
  }

  override def run(args: List[String]): IO[ExitCode] = program.as(ExitCode.Success)
}

// try fibers

package com.evolutiongaming.bootcamp.effects.v3

import cats.effect.{ExitCode, IO, IOApp, Resource}
import com.evolutiongaming.bootcamp.effects.v3.ResourceExample.DBModule.DBService
import com.evolutiongaming.bootcamp.effects.v3.ResourceExample.KafkaModule.KafkaService

import java.io.FileInputStream
import scala.concurrent.duration._
import scala.io.{BufferedSource, Source}

/*
 * `Resource` allows to effectfully allocate and release a resource.
 *
 * This is used to make sure resources are released (e.g., files and database connections are closed),
 * no matter what, thus avoiding resource leaks.
 *
 * Refer also to https://typelevel.org/cats-effect/docs/2.x/datatypes/resource
 */
object ResourceApp extends IOApp {

  def acquire(name: String): IO[BufferedSource] = IO(Source.fromFile(name))
  def release(source: BufferedSource): IO[Unit] = IO(source.close())

  def readSource(source: Source): IO[Iterator[String]] = IO(source.getLines())

  /*
   * `make` to create a resource providing an acquire and release functions.
   */
  def fileResource(name: String): Resource[IO, Source] =
    Resource.make(acquire(name))(release)

  /*
   * `Resource.fromAutoCloseable` creates a resource from any
   * https://docs.oracle.com/en/java/javase/15/docs/api/java.base/java/lang/AutoCloseable.html
   */
  def fileInputStreamResource(name: String): Resource[IO, FileInputStream] =
    Resource.fromAutoCloseable(IO(new FileInputStream(name)))

  def fileInputStreamBlockingResource(name: String): Resource[IO, FileInputStream] =
    Resource.fromAutoCloseable(IO.blocking(new FileInputStream(name)))

  def resourceProgram: IO[Unit] =
    fileResource("ReadMe.md")
      .evalMap(readSource)
      .map(_.mkString("\n"))
      .use(str => IO.delay(println(str)))

  def filesProgram: IO[Unit] = ???

  def run(args: List[String]): IO[ExitCode] =
    resourceProgram.as(ExitCode.Success)
}

object InitializationOrder extends IOApp {

  def workflow(action: String): IO[Unit] =
    IO(println(s"$action started")) *> IO.sleep(1.second) *> IO(println(s"$action finished"))

  def workflowResource: Resource[IO, Unit] =
    for {
      _ <- Resource.make(workflow("R1 Acquire"))(_ => workflow("R1 Release"))
      _ <- Resource.make(workflow("R2 Acquire"))(_ => workflow("R2 Release"))
      _ <- Resource.make(workflow("R3 Acquire"))(_ => workflow("R3 Release"))
    } yield ()

  def run(args: List[String]): IO[ExitCode] =
    workflowResource.use(_ => workflow("USE")).as(ExitCode.Success)
}

object ResourceExample extends IOApp {

  def workflow(action: String): IO[Unit] =
    IO(println(s"$action started")) *> IO.sleep(1.second) *> IO(println(s"$action finished"))

  object KafkaModule {
    type KafkaService = Unit
    def make: Resource[IO, KafkaService] =
      Resource.make(
        workflow("Acquire Kafka")
      )(_ => workflow("Release Kafka"))
  }

  object DBModule {
    type DBService = Unit
    def make: Resource[IO, DBService] =
      Resource.make(
        workflow("Acquire DB")
      )(_ => workflow("Release Db"))
  }

  object HttpClientModule {
    type HttpClient = Unit
    def make(kafka: KafkaService, dbService: DBService): Resource[IO, HttpClient] =
      Resource.make(
        workflow("Acquire HttpClient")
      )(_ => workflow("Release HttpClient"))
  }

  def run(args: List[String]): IO[ExitCode] =
    (for {
      kafka <- KafkaModule.make
      db    <- DBModule.make
      _     <- HttpClientModule.make(kafka, db)
    } yield ())
      .use(_ => workflow("USE"))
      .as(ExitCode.Success)
}

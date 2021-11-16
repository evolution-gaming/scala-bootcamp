package com.evolutiongaming.bootcamp.effects.v3

import cats.effect.{Blocker, ExitCase, ExitCode, IO, IOApp, Resource}
import cats.syntax.all._
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

  /*
   * `Resource.fromAutoCloseableBlocking` also takes a `Blocker` (a blocking context to use
   * for acquire and release operations).
   *
   * `Blocker`-s will be further discussed in `ContextShift`-s section.
   */
  def fileInputStreamBlockingResource(name: String, blocker: Blocker): Resource[IO, FileInputStream] =
    Resource.fromAutoCloseableBlocking(blocker)(IO(new FileInputStream(name)))

  def resourceProgram: IO[Unit] =
    fileResource("ReadMe.md")
      .evalMap(readSource) // <- Works in the same way as `flatMap` for F[A]
      .map(_.mkString("\n"))
      .use(str => IO.delay(println(str)))

//  def cantUseAgainOnceUsed: IO[Unit] =
//    fileResource("ReadMe.md")
//      .use(source => IO.pure(source))
//      .flatMap(readSource)
//      .map(_.mkString("\n"))
//      .flatMap(str => IO.delay(println(str)))

//  acquire("ReadMe.md").bracket { file1 =>
//    acquire("ReadMe.md").bracket { file2 =>
//      acquire("ReadMe.md").bracket { file3 =>
//        (for {
//          source1 <-  readSource(file1)
//          source2 <- readSource(file2)
//          source3 <- readSource(file3)
//        } yield source1 ++ source2 ++ source3)
//          .map(_.filter(_.toLowerCase.contains("evolution")).mkString("\n"))
//          .flatMap(str => IO.delay(println(str)))
//      }(release)
//    }(release)
//  }(release)

  def filesProgram: IO[Unit] =
    (for {
      r1 <- fileResource("ReadMe.md")
      r2 <- fileResource("ReadMe.md")
      r3 <- fileResource("ReadMe.md")
    } yield (r1, r2, r3)).use {
      case (r1, r2, r3) =>
        (for {
          lines1 <- readSource(r1)
          lines2 <- readSource(r2)
          lines3 <- readSource(r3)
        } yield lines1 ++ lines2 ++ lines3)
          .map(_.filter(_.toLowerCase.contains("evolution")).mkString("\n"))
          .flatMap(str => IO.delay(println(str)))
    }

  def run(args: List[String]): IO[ExitCode] =
//    resourceProgram.as(ExitCode.Success)
    filesProgram.as(ExitCode.Success)
//    cantUseAgainOnceUsed.as(ExitCode.Success)

  val intValue             = 42
  val a: Resource[IO, Int] = Resource.pure[IO, Int](intValue)

  val pureIO: IO[Int]      = IO.pure(intValue) // A -> F[A] /  object -> def of: F[A]
//  val b = pureIO.toResource // F[A] -> Resource[F, A]
  val c: Resource[IO, Int] = Resource.eval(pureIO)
  Resource.make(pureIO)(_ => IO.unit)

  Resource.makeCase(pureIO)((_, exitCase) =>
    exitCase match {
      case ExitCase.Completed => ???
      case ExitCase.Error(_)  => ???
      case ExitCase.Canceled  => ???
    }
  )
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

  def cancelProgram: IO[Unit] =
    for {
      fiber <- Resource
                 .make(workflow("Acquire"))(_ => workflow("Release"))
                 .use(_ => workflow("USE")) // Resource[F, A] => IO[B]
                  /// <- ALL RESOURCE RELEASED
                 .start
      _     <- IO.sleep(500.millis) *> fiber.cancel
      _     <- IO.sleep(5.seconds)
    } yield ()

//  def failureResource: Resource[IO, Unit] =
//    for {
//      _ <- Resource.make(workflow("R1 Acquire"))(_ => workflow("R1 Release"))
//      _ <- Resource.make(workflow("R2 Acquire"))(_ => workflow("R2 Release"))
//    } yield ()

  def failureResource: Resource[IO, Unit] =
    for {
      _ <- Resource.make(workflow("R1 Acquire"))(_ => workflow("R1 Release"))
      _ <- Resource.make(workflow("R2 Acquire"))(_ =>
             IO.delay(println("R2 Release is about to die")) *> IO.raiseError(new RuntimeException("error"))
           )
    } yield ()

  def run(args: List[String]): IO[ExitCode] =
//    cancelProgram.as(ExitCode.Success)
//    workflowResource.use(_ => workflow("USE")).as(ExitCode.Success)
    failureResource
      .use(_ => workflow("USE"))
      .handleErrorWith(e => IO.delay(println(s"Resource release failed with: ${e.getMessage}")))
      .as(ExitCode.Success)
}

object ResourceExample extends IOApp {

  def workflow(action: String): IO[Unit] =
    IO(println(s"$action started")) *> IO.sleep(1.second) *> IO(println(s"$action finished"))

  object KafkaModule {
    type KafkaService = Unit
    type RngService = Unit // F[Int]

//    def apply: KafkaService = ???
//    def of(rngService: RngService): IO[KafkaService] = ???

    def make: Resource[IO, KafkaService] = // F[A] -> def of / apply / fromString / fromBuffer / fromDB -> A
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
//      kafka <- Resource.eval(KafkaModule.of(???))
      _ <- Resource.eval(IO.raiseError(new RuntimeException("error")))
      db    <- DBModule.make
      _     <- HttpClientModule.make(kafka, db)
    } yield ())
      .use(_ => workflow("USE"))
      .as(ExitCode.Success)
}

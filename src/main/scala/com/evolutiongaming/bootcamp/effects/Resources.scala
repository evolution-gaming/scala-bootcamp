package com.evolutiongaming.bootcamp.effects

import java.io.FileInputStream

import cats.effect._
import com.evolutiongaming.bootcamp.effects.Console.Real.putStrLn

import scala.io.{BufferedSource, Source}

object Resources extends IOApp {
  /*
   * `Resource` allows to effectfully allocate and release a resource.
   *
   * This is used to make sure resources are released (e.g., files and database connections are closed),
   * no matter what, thus avoiding resource leaks.
   *
   * Refer also to https://typelevel.org/cats-effect/datatypes/resource.html.
   */
  private def acquire(name: String): IO[BufferedSource] = IO(Source.fromFile(name))
  private def release(source: BufferedSource): IO[Unit] = IO(source.close())

  /*
   * `make` to create a resource providing an acquire and release functions.
   */
  private def fileResource(name: String): Resource[IO, Source] = Resource.make(acquire(name))(release)

  private def readSource(source: Source): IO[Iterator[String]] = IO(source.getLines())

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
  def fileInputStreamBlockingResource(blocker: Blocker): Resource[IO, FileInputStream] =
    Resource.fromAutoCloseableBlocking(blocker)(IO(new FileInputStream("ReadMe.md")))

  def program(name: String): IO[Unit] =
    fileResource(name)
      .evalMap(readSource)
      .map(_.mkString("\n"))
      .use(putStrLn)

  override def run(args: List[String]): IO[ExitCode] = {
    args match {
      case name :: Nil  => program(name)
      case x            => putStrLn(s"Invalid arguments: $x")
    }
  } as ExitCode.Success
}

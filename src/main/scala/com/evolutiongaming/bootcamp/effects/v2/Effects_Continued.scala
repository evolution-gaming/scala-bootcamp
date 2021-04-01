package com.evolutiongaming.bootcamp.effects.v2

import java.io.{BufferedReader, FileReader}
import java.util.concurrent.Executors

import cats.effect.{Blocker, ContextShift, ExitCase, ExitCode, IO, IOApp, Resource, Sync}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object Cats_Resource_Example1 extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val intValue = 123
    val pureIO = IO.pure(intValue)

    Resource.pure[IO, Int](intValue)
    Resource.liftF(pureIO)

    Resource.make(pureIO)(_ => IO.unit)
    Resource.makeCase(pureIO)((_, exitCase) =>
      exitCase match {
        case ExitCase.Completed => ???

        case ExitCase.Error(_) => ???

        case ExitCase.Canceled => ???
      })

    val fileName = "file_name"
    Resource.fromAutoCloseable(
      IO(
        new BufferedReader(
          new FileReader(fileName)
        )
      )
    )
    Resource.apply(IO.pure[(Int, IO[Unit])]((intValue, IO.unit)))

    for {
      fiber <- Resource.make(
        IO(println("Acquire Start")) *>
          IO.sleep(1.second) *>
          IO(println("Acquire Finish"))
      )(_ =>
        IO(println("Release Start")) *>
          IO.sleep(1.second) *>
          IO(println("Release Finish"))
      ).use(_ =>
        IO(println("Operation Start")) *>
          IO.sleep(1.second) *>
          IO(println("Operation Finish"))
      ).start
      _ <- IO.sleep(500.milliseconds) *> fiber.cancel
      _ <- IO.sleep(5.second)
    } yield ExitCode.Success


    //    def recursiveF(v: IO[Int]): IO[Int] =
    //      v.flatMap(v => IO(println(v)) *> recursiveF(IO.pure(v + 1)))
    //
    //    // IO.sleep, IO.shift, 256 flatMap == IO.shift, IO.cancelBoundary
    //    for {
    //      fiber <- Resource.make(
    //        IO(println("Acquire Start")) *>
    //          IO.sleep(1.second) *>
    //          IO(println("Acquire Finish"))
    //      )(_ =>
    //        IO(println("Release Start")) *>
    //          IO.sleep(1.second) *>
    //          IO(println("Release Finish"))
    //      ).use(_ =>
    //        recursiveF(IO.pure(0))
    //        //IO(println("Operation Start")) *> IO.sleep(10.milliseconds) *> IO(println("Release Finish"))
    //        //IO(println("Operation Start")) *> IO.shift *> IO(println("Release Finish"))
    //      ).start
    //      _ <- IO.sleep(500.milliseconds) *> fiber.cancel
    //      _ <- IO.sleep(5.second)
    //    } yield ExitCode.Success

  }

}

object Cats_Resource_Example2 extends IOApp {

  object KafkaModule {
    type KafkaService = Unit
    def of: Resource[IO, KafkaService] =
      Resource.make(
        IO(println("Acquire Kafka Start")) *>
          IO.sleep(1.second) *>
          IO(println("Acquire Kafka Finish"))
      )(_ =>
        IO(println("Release Kafka Start")) *>
          IO.sleep(1.second) *>
          IO(println("Release Kafka Finish"))
      )
  }

  object DBModule {
    type DBService = Unit
    def of: Resource[IO, DBService] =
      Resource.make(
        IO(println("Acquire DB Start")) *>
          IO.sleep(1.second) *>
          IO(println("Acquire DB Finish"))
      )(_ =>
        IO(println("Release DB Start")) *>
          IO.sleep(1.second) *>
          IO(println("Release DB Finish"))
      )
  }

  object HttpClientModule {
    type HttpClient = Unit
    def of: Resource[IO, HttpClient] =
      Resource.make(
        IO(println("Acquire HttpClient Start")) *>
          IO.sleep(1.second) *>
          IO(println("Acquire HttpClient Finish"))
      )(_ =>
        IO(println("Release HttpClient Start")) *>
          IO.sleep(1.second) *>
          IO(println("Release HttpClient Finish"))
      )
  }

  override def run(args: List[String]): IO[ExitCode] = (for {
    _ <- KafkaModule.of
    //_ <- Resource.liftF(IO.raiseError[Unit](new RuntimeException))
    _ <- DBModule.of
    _ <- HttpClientModule.of
  } yield ExitCode.Success)
    .use(IO.pure)

}


object Cats_Resource_Exercise1 extends IOApp {

  // Open files, compare contents by length, update the file with the least amount of data.
  def merge(filePath1: String, filePath2: String): IO[Unit] = ???

  override def run(args: List[String]): IO[ExitCode] = ???

}


/*
 * https://typelevel.org/cats-effect/docs/2.x/datatypes/contextshift
 *
 * ContextShift is the pure equivalent to:
 *    Scala's ExecutionContext
 *    Java's Executor
 *    JavaScript's setTimeout(0) or setImmediate
 */
object Cats_ContextShift_Example1 extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val executionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
    val cs = IO.contextShift(executionContext)
    cs.shift
    cs.evalOn(executionContext)(IO.pure(123))

    for {
      _ <- IO(println(s"operation - ${ Thread.currentThread().getName }"))
      //_ <- IO.shift(cs)
      //_ <- contextShift.evalOn(executionContext)(IO(println(s"operation - ${Thread.currentThread().getName}")))
      _ <- IO(println(s"operation - ${ Thread.currentThread().getName }"))
      _ <- IO(println(s"operation - ${ Thread.currentThread().getName }"))
    } yield ExitCode.Success

  }

}

object Cats_ContextShift_Exercise1 extends IOApp {

  def readName(blocker: Blocker)(implicit contextShift: ContextShift[IO], sync: Sync[IO]): IO[String] = ???
  def readAge(blocker: Blocker)(implicit contextShift: ContextShift[IO], sync: Sync[IO]): IO[String] = ???

  override def run(args: List[String]): IO[ExitCode] = ???
}


object Cats_ContextShift_Example2 extends IOApp {

  def readName(blocker: Blocker)(implicit contextShift: ContextShift[IO], sync: Sync[IO]): IO[String] =
    blocker.delay {
      println(s"operation - ${ Thread.currentThread().getName }")
      println("Enter your name: ")
      scala.io.StdIn.readLine()
    }

  def run(args: List[String]) = {
    //val executionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
    //val cs = IO.contextShift(executionContext)
    val name = Blocker[IO].use { blocker =>
      //readName(blocker)(cs, implicitly[Sync[IO]])
      readName(blocker)
    }

    for {
      n <- name
      _ <- IO(println(s"operation - ${ Thread.currentThread().getName }"))
      _ <- IO(println(s"Hello, $n!"))
    } yield ExitCode.Success
  }

}

object Cats_ContextShift_Exercise2 extends IOApp {

  def printInfo(v: Int, operationName: String): IO[Unit] =
    IO(println(s"value - $v, operation name - $operationName, ${ Thread.currentThread().getName }"))

  // recursive increment up to 1024, call printInfo at each step.
  def recursiveF(v: Int, operationName: String): IO[Int] = ???

  override def run(args: List[String]): IO[ExitCode] = {
    val executionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))
    val cs = IO.contextShift(executionContext)

    for {
      _ <- recursiveF(0, "operation 1").start(cs)
      _ <- recursiveF(0, "operation 2").start(cs)
      _ <- IO.sleep(15.second)
    } yield ExitCode.Success
  }

}

/*
  https://typelevel.org/cats-effect/docs/2.x/datatypes/fiber
  It represents the (pure) result of an Async data type (e.g. IO) being started concurrently and that can be either joined or canceled.
  You can think of fibers as being lightweight threads, a fiber being a concurrency primitive for doing cooperative multi-tasking.

  `Fiber` is just a handle over a runloop
  `join`: semantically blocks for completion (via `Deferred` and ultimately `Ref` + `async`)
  `cancel`: interruption (runloop stops running on a signal, out of scope)
 */
object Cats_Fiber_Example1 extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    ???
  }

}

object Cats_Fiber_Exercise1 extends IOApp {

  def readIndex(blocker: Blocker)(implicit contextShift: ContextShift[IO], sync: Sync[IO]): IO[Int] = ???
  def getDataFromDB(index: Int): IO[Int] =
    IO.sleep(5.second) *> IO.pure(index * 2)

  override def run(args: List[String]): IO[ExitCode] = {
    ???
  }

}


/*
  Additional assignment:
  1. Read from the console the file path.
    1.1 Use Blocking Thread Pool
    1.2 Check the transmitted data(Error Handling + Validation).
  2. Read from the console the seed.
    2.1 Use Blocking Thread Pool
    2.2 Check the transmitted data(Error Handling + Validation).
  3. Read the data from the file.
  4. Calculate the signature (in parallel if possible).
    4.1 Use Separate Thread Pool(ContextShift)
    4.2 Split text into words
    4.3 Calculate hash for each word
    4.4 Take the minimal hash
    4.5* Repeat the process for n different hash functions.
  5. Save the signature in memory(think about storage).
  6. Terminate the application.

  def javaHash(word: String, seed: Int = 0): Int = {
    var hash = 0

    for (ch <- word.toCharArray)
      hash = 31 * hash + ch.toInt

    hash = hash ^ (hash >> 20) ^ (hash >> 12)
    hash ^ (hash >> 7) ^ (hash >> 4)
  }

  def knuthHash(word: String, constant: Int): Int = {
    var hash = 0
    for (ch <- word.toCharArray)
      hash = ((hash << 5) ^ (hash >> 27)) ^ ch.toInt
    hash % constant
  }
 */

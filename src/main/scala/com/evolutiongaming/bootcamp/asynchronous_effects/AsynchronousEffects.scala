package com.evolutiongaming.bootcamp.asynchronous_effects

import cats.effect.{ExitCode, IO, IOApp}

import scala.io.StdIn
import cats.implicits._

import scala.annotation.tailrec

/*
 * Side effects - Modifying or accessing shared state outside the local environment - producing an
 *                observable effect other than returning a value to the invoker.
 *
 *                For example - updating a global variable, writing/reading to disk/console.
 *
 * But don't we need to do these "side effects" to write useful programs?
 *
 * Yes, indeed we do.
 *
 * In functional programming we wrap side effects into "IO Monads":
 *  - This turns them into data which can be passed around to functions
 *  - Keeps referential transparency so it is easier to refactor our programs and reason about them
 *  - We can evaluate them when we want
 *  - Can be sequentially executed
 *  - Can be used in `for`-comprehensions
 *
 * There are a number of popular libraries providing IO Monads in the Scala ecosystem, each with their
 * pros & cons:
 *  - Cats Effect - https://typelevel.org/cats-effect/ - versions 2 and 3
 *  - ZIO - https://zio.dev/
 *  - Monix - https://monix.io/
 *
 *  There are also ways how to write software without being tied to a particular IO Monad ("Tagless Final"
 *  pattern), but we will not discuss this in this lecture.
 */
object Exercise1_Imperative {
  import com.evolutiongaming.bootcamp.asynchronous_effects.Exercise1_Common.response
  private var counter: Int = 0

  @tailrec
  def main(args: Array[String]): Unit = {
    println("What is your favourite animal?")
    val animal = StdIn.readLine()
    val output = response(animal)
    output match {
      case Some(x)  =>
        println(x)

      case None     =>
        if (counter >= 2) {
          println("I am disappoint. You have failed to answer too many times.")
          sys.exit(1)
        } else {
          counter += 1
          println("Empty input is not valid, try again...")
          main(args)
        }
    }
  }

  // Question: How do you test this?
  // Question: How do you refactor this to retry upon empty input at most 3 times?
}

object Exercise1_Common {
  def response(animal: String): Option[String] = animal.trim match {
    case "cat" | "cats"   =>  "In ancient times cats were worshipped as gods; they have not forgotten this.".some
    case "dog" | "dogs"   =>  "Be the person your dog thinks you are.".some
    case x if x.nonEmpty  =>  s"I don't know what to say about '$x'.".some
    case _                =>  none
  }

  trait Console {
    def writeString(value: String): IO[Unit]
    def readString: IO[String]
  }

  /*
   * Exercise 1a. Implement `RealConsole` using `IO.apply` and `IO.unit`.
   */
  final object RealConsole extends Console {
    // TODO: replace both with `???` after code review
    def writeString(value: String): IO[Unit] = IO(println(value))
    def readString: IO[String] = IO(StdIn.readLine())
  }
}

/*
 * Exercise 1b. Re-implement Exercise1_Imperative avoiding side-effecting code using the "IO Monad"
 *
 * Use:
 *  - `for`-comprehension
 *  - `IO.as` as a `map` which discards the first result to return `ExitCode`-s
 *  - `*>` as a `flatMap` which discards the first result to sequence `IO[Unit]` with another `IO`
 *  - Tests in `AsynchronousEffectsSpec` to check your work
 */
object Exercise1_Functional extends IOApp {
  import Exercise1_Common._

  // TODO: replace with `???` after code review
  def process(console: Console, counter: Int = 0): IO[ExitCode] = {
    import console._

    for {
      _ <- writeString("What is your favourite animal?")
      animal      <-  readString
      output      =   response(animal)
      result      <-  output match {
                        case None =>
                          if (counter >= 2) {
                            writeString("I am disappoint. You have failed to answer too many times.") as ExitCode.Error
                          } else {
                            writeString("Empty input is not valid, try again...") *> process(console, counter + 1)
                          }

                        case Some(x) =>
                          writeString(x) as ExitCode.Success
                      }
    } yield result
  }

  override def run(args: List[String]): IO[ExitCode] = {
    process(RealConsole)
  }
}

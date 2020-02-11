package com.evolutiongaming.bootcamp.error_handling

import java.io.{BufferedReader, FileReader}

import scala.util.{Failure, Success, Try}

// https://medium.com/@dkomanov/scala-try-with-resources-735baad0fd7d
object TryWithResource extends App {

  def tryWithResource1[R >: Null, A](resource: R)(use: R => A)(close: R => Unit): A = {
    try {
      use(resource)
    } finally {
      close(resource)
    }
  }

  def tryWithResource2[R >: Null, A](resource: R)(use: R => A)(close: R => Unit): A = {
    var exception: Throwable = null
    try {
      use(resource)
    } catch {
      case error: Throwable =>
        exception = error
        throw exception
    } finally {
      try {
        close(resource)
      } catch {
        case error: Throwable =>
          if (exception != null) exception.addSuppressed(error)
          else throw error
      }
    }
  }

  // Exercise
  // Try to implement this class
  // NB: remember of fatal errors
  // Question: why open is a by-name parameter?
  final class Resource[R](open: => R, close: R => Unit) {

    def use[A](f: R => A): Try[A] = ???
  }

//  val resource = new Resource[BufferedReader](
//    new BufferedReader(new FileReader("./src/main/scala/com/evolutiongaming/bootcamp/error_handling/TryWithResource.scala")),
//    _.close()
//  )
//
//  val content = resource.use { reader =>
//    List.unfold(reader.readLine()) { line =>
//      Option(line).map(line => (line, reader.readLine()))
//    }.mkString("\n")
//  }
//
//  println(content)
}

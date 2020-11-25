package com.evolution.bootcamp.inspect

import cats.data.State
import org.scalatest.funsuite.AnyFunSuite

class PrinterSpec extends AnyFunSuite {

  type F[T] = State[String, T]

  implicit val console: Console[F] = new Console[F] {
    def putStrLn(text: String) = State.modify(_ + s"$text\n")
  }
  val printer = Printer.create[F]

  test("we are printing something about camel") {
    val initialState = ""
    val resultingState = printer.print.runS(initialState).value
    assert(resultingState contains "Camel")
  }


}

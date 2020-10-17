package com.evolutiongaming.bootcamp.testing2

import Calculator._

/** Simple calculator with buttons.
  *
  * @param memory whatever is stored in the memory.
  * @param screen whatever you see on the screen.
  */
case class Calculator(memory: Int = 0, screen: Int = 0, operation: Option[Operation] = None) {

  def enter(digit: Int): Either[String, Calculator] =
    if (digit >= 0 && digit <= 9) {
      Right(this.copy(memory = memory * 10 + digit))
    } else {
      Left("digit out of range")
    }

  def plus: Calculator = this.copy(operation = Some(Operation.Plus))

  def calculate: Calculator = operation.fold(this) {
    case Operation.Plus => Calculator(memory = 0, screen = screen + memory)
  }

}
object Calculator {
  sealed trait Operation
  object Operation {
    object Plus extends Operation
  }
}
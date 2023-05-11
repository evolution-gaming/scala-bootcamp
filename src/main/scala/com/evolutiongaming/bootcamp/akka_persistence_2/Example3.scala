package com.evolutiongaming.bootcamp.akka_persistence_2

/** A - type of our aggregate / state, like EmployeeBasket or EmployeeAccount
  * C - Commands
  * E - Events
  * R - Errors / Rejections
  */
trait AggregateBehaviour[A, C, E, R] {

  def processCommand(state: A): C => Either[R, List[E]]

  def applyEvent(state: A): E => A

}

// let's define our EmployeeBasket commands

sealed trait BasketCommand
final case class AddItem(entityId: String, item: String) extends BasketCommand

// And events
sealed trait BasketEvent
final case class ItemAdded(item: String) extends BasketEvent

// Rejections
sealed trait BasketError
case object BasketFull extends BasketError

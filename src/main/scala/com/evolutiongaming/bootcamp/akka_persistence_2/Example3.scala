package com.evolutiongaming.bootcamp.akka_persistence_2

/**
 * A - type of our aggregate / state, like EmployeeBasket or EmployeeAccount
 * C - Commands
 * E - Events
 * R - Errors / Rejections
 */
trait AggregateBehaviour[A, C, E, R] {

  def processCommand

  def applyEvent

}

// let's define our EmployeeBasked commands

sealed trait BasketCommand

// And events

sealed trait BasketEvent

// Rejections

sealed trait BasketError


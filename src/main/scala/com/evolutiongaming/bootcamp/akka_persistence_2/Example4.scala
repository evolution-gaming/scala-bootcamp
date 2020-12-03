package com.evolutiongaming.bootcamp.akka_persistence_2

import akka.persistence.{PersistentActor, RecoveryCompleted, SnapshotOffer}

case class EmployeeBasket(
  employeeId: String,
  items: List[Item]
)

case class Item(id: String, name: String, description: String)

class BasketAggregateBehaviour extends AggregateBehaviour[EmployeeBasket, BasketCommand, BasketEvent, BasketError] {

  override def processCommand: Unit = ???

  override def applyEvent: Unit = ???

}

object Example4 extends App {


  class EmployeeShoppingBasketActor extends PersistentActor {

    def id = self.path.name

    val behaviour = new BasketAggregateBehaviour

    private var basket = EmployeeBasket(id, Nil)

    override def receiveRecover: Receive = {
      case SnapshotOffer(metadata, basketFromSnapshot: EmployeeBasket) =>
        basket = basketFromSnapshot

      case event: BasketEvent =>
        ???

    }

    override def receiveCommand: Receive = {
      case cmd: BasketCommand =>
        ???
    }

    // user id, we will see how to deal with it in future
    override def persistenceId: String = "user-123"

  }

}

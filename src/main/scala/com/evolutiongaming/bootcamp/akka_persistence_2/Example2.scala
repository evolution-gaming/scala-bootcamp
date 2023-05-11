package com.evolutiongaming.bootcamp.akka_persistence_2

import akka.actor.{ActorSystem, PoisonPill, Props}
import akka.persistence.{PersistentActor, RecoveryCompleted, SnapshotOffer}

import scala.io.StdIn

object Example2 extends App {

  class EmployeeShoppingBasketActor extends PersistentActor {

    // state
    private var basket = List.empty[String]

    override def receiveRecover: Receive = {

      // During recovery, the persistent actor is offered the latest saved snapshot via a SnapshotOffer message
      // from which it can initialize internal state.
      // SnapshotMetadata: persistenceId, sequenceNr, timestamp
      case SnapshotOffer(metadata, basketFromSnapshot: List[String]) =>
        println("Snapshot received: " + basketFromSnapshot)
        println("Last stored event: " + metadata.sequenceNr)
        basket = basketFromSnapshot

      case RecoveryCompleted =>
        println("Recovered state " + basket)
      // log that we covered from snapshot successfully
      // measure recovery time

      case event: String =>
        basket = event :: basket
        println(s"Received $event")
    }

    override def receiveCommand: Receive = { case item: String =>
      persist(item) { e =>
        basket = item :: basket // storing item to basket
        println(s"Storing event $e")
        // As we only add items, in sake of simplicity, let's store snapshot each 5th item.
        if (basket.size % 5 == 0) {
          println(s"Storing snapshot on event $e")
          saveSnapshot(basket)
        }
      }
    }

    override def persistenceId: String = "user-121"

  }

  val system = ActorSystem("AkkaPersistenceSystem")
  val basket = system.actorOf(Props[EmployeeShoppingBasketActor]())

  StdIn.readLine()

  basket ! "banana"
  basket ! "apple"
  basket ! "orange"
  basket ! "avocado"
  basket ! "cherries" // <-- snapshot should be created at this point
  basket ! "guava"

  StdIn.readLine()

  basket ! PoisonPill

  // latest snapshot will be taken
  // but you can control snapshot behaviour with Recovery strategy

  val basket2 = system.actorOf(Props[EmployeeShoppingBasketActor]())

  basket2 ! "plum"

  StdIn.readLine()
}

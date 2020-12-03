package com.evolutiongaming.bootcamp.akka_persistence_2

import akka.actor.{ActorSystem, PoisonPill, Props}
import akka.persistence.PersistentActor

import scala.io.StdIn

object Example1 extends App {

  class EmployeeShoppingBasketActor extends PersistentActor {

    override def receiveRecover: Receive = {
      case event =>
        println(s"Received $event")
    }

    override def receiveCommand: Receive = {
      case message =>

        // There are also:
        // persistAll
        // persistAllAsync
        // persistAsync
        persist(message) { e =>
          println(s"Storing event $e")
        }
    }

    // user id, we will see how to deal with it in future
    override def persistenceId: String = "user-123"

  }

  val system = ActorSystem("AkkaPersistenceSystem")
  val basket = system.actorOf(Props[EmployeeShoppingBasketActor])

  StdIn.readLine()

  basket ! "banana"
  basket ! "apple"

  StdIn.readLine()

  basket ! PoisonPill

  val basket2 = system.actorOf(Props[EmployeeShoppingBasketActor]())
  basket2 ! "orange"

  StdIn.readLine()

}

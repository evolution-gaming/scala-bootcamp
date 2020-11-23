package com.evolutiongaming.bootcamp.akka.actors

import akka.actor.{Actor, ActorSystem, Props}

object Broadcasting extends App {

  case object Ping
  case object Pong

  final class Child extends Actor {
    override def receive: Receive = {
      case Ping =>
        context.parent ! Pong
    }
  }

  class Parent extends Actor {
    private val child1 = context.actorOf(Props[Child](), "child1")
    private val child2 = context.actorOf(Props[Child](), "child2")

    override def receive: Receive = {
      case Parent.PingChildren =>
        context.children.foreach(_ ! Ping)

      case Parent.PingSelection =>
        val selection = context.actorSelection("/user/parent/child*")
        // selection.resolveOne() returns Future[ActorRef]
        // ? cannot be used
        selection ! Ping

      case Pong =>
        println(s"pong from ${sender()}")
    }
  }

  object Parent {
    case object PingChildren
    case object PingSelection
  }

  val evoActorSystem: ActorSystem = ActorSystem("evo-actor-system")
  val parent = evoActorSystem.actorOf(Props[Parent](), "parent")

  parent ! Parent.PingChildren
  parent ! Parent.PingSelection

}

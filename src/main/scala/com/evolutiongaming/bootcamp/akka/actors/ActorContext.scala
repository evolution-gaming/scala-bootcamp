package com.evolutiongaming.bootcamp.akka.actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

// an actor can do more things than just send messages.
// it can create other actors, and it can change its behavior.
// all this things are available via actor context
object ActorContext extends App {

  // Behavior switch
  // Each actor has a stack of behaviors and the top most one is always the acted one.

  final class CounterVar extends Actor {
    private var acc: Int = 0

    override def receive: Receive = {
      case "inc" => acc += 1
      case "get" => sender() ! acc
    }
  }

  // functionally equivalent to the previous version
  final class CounterContextBecome extends Actor {
    // note: we don't use var here

    // ~ a tail-recursive function, but it is asynchronous
    // state change is explicit and state is scoped to current behavior
    // we can change actor behavior for the next message
    private def counter(acc: Int): Receive = {
      case "inc" => context.become(counter(acc + 1))
      case "get" => sender() ! acc
    }

    override def receive: Receive = counter(0)
  }

  // Creating and stopping actors

  // application as an actor
  final class Main extends Actor {
    // an actor is created by exactly one other actor
    private val counter: ActorRef = context.actorOf(Props[CounterContextBecome](), "counter")

    (1 to 10).foreach(_ => counter ! "inc")
    counter ! "get"

    override def receive: Receive = {
      case count: Int =>
        println(s"count = $count")
        context.stop(self)
    }
  }

  val evoActorSystem: ActorSystem = ActorSystem("evo-actor-system")
  evoActorSystem.actorOf(Props[Main]())

}

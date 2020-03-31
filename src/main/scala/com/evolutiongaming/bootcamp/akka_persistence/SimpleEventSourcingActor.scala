package com.evolutiongaming.bootcamp.akka_persistence

import akka.actor.{Actor, ActorRef}

class SimpleEventSourcingActor extends Actor {
  override def receive: Receive = ???
}

object SimpleEventSourcingActor {
  sealed trait Command
  case class Event(entity: Any) extends Command
  case class Subscribe(subscriber: ActorRef, channel: Class[_]) extends Command
  case class Unsubscribe(subscriber: ActorRef, channel: Class[_]) extends Command
}

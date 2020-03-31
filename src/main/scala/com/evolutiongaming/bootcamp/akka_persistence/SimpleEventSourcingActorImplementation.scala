package com.evolutiongaming.bootcamp.akka_persistence

import akka.actor.{Actor, ActorRef}
import SimpleEventSourcingActor.{Event, Subscribe, Unsubscribe}

import scala.collection.mutable.Map

class SimpleEventSourcingActorImplementation extends Actor {

  val state: Map[Class[_], List[ActorRef]] = Map.empty

  override def receive: Receive = {
    case entity: Subscribe =>
      state.update(
        entity.channel,
        state.getOrElse(entity.channel, List.empty) :+ entity.subscriber
      )

    case entity: Unsubscribe =>
      state.update(
        entity.channel,
        state.getOrElse(entity.channel, List.empty)
          .filterNot(_ == entity.subscriber)
      )

    case event: Event =>
      state.get(event.entity.getClass)
        .foreach(_.foreach(_ ! event.entity))
  }

}

object SimpleEventSourcingActorImplementation {
  sealed trait Command
  case class Event(entity: Any) extends Command
  case class Subscribe(subscriber: ActorRef, channel: Class[_]) extends Command
  case class Unsubscribe(subscriber: ActorRef, channel: Class[_]) extends Command
}

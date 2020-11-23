package com.evolutiongaming.bootcamp.akka.actors

import java.time.Instant

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.evolutiongaming.bootcamp.akka.actors.EventStream.AlertListener.Alert

// ~ event bus
object EventStream extends App {
  val evoActorSystem: ActorSystem = ActorSystem("evo-actor-system")

  // listener
  final class AlertListener extends Actor with ActorLogging {
    override def receive: Receive = {
      case Alert(time, source) =>
        log.warning(s"something bad happened from $source at $time")
    }
  }
  object AlertListener {
    final case class Alert(source: String, time: Instant = Instant.now())
  }

  val alertListener = evoActorSystem.actorOf(Props[AlertListener]())

  // subscription
  evoActorSystem.eventStream.subscribe(alertListener, classOf[Alert])

  // publisher
  final class ProcessingActor extends Actor {
    override def receive: Receive = {
      case x: Int if x < 0 => context.system.eventStream.publish(Alert(s"processing: negative number $x"))
      case _               => // nothing
    }
  }

  val integrationActor = evoActorSystem.actorOf(Props[ProcessingActor]())

  integrationActor ! "asasd"
  integrationActor ! 63
  integrationActor ! -56
  integrationActor ! -3
}

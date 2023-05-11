package com.evolutiongaming.bootcamp.akka.actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

// actor = function (receive) + state (var) + lifecycle + supervisor strategy
object AkkaHelloWorld extends App {
  // actors are defined as instances of a class that extends the `akka.actor.Actor` trait
  class PlayerProfile(initialName: String) extends Actor {
    // internal state
    // do not worry, cannot be accessed outside the actor
    var name: String = initialName

    // need to define only 1 method: `receive` is responsible for handling incoming messages.
    // `receive` matches incoming messages to the appropriate behavior
    override def receive: Receive = {
      case PlayerProfile.UpdateName(newName) => name = newName
      case PlayerProfile.LogName             => println(s"my name is $name")
      case unknown: Any                      => println(s"unknown: $unknown")
    }
  }

  object PlayerProfile {
    // incoming messages, any messages
    case class UpdateName(name: String)
    case object LogName
  }

  // runtime for actors
  val system: ActorSystem = ActorSystem("player-system")

  // configuration object (basically we need actor type and deployment config)
  // immutable and can be used as a message
  val props = Props(classOf[PlayerProfile], "unknown")

  // another way to specify props
  // should not be used within another actor
  val props1 = Props(new PlayerProfile("unknown"))

  // ActorRef is an immutable handle (remote controller) to an actor
  val playerProfile: ActorRef = system.actorOf(props, "player-profile")

  // send message to our actorRef
  // it simply adds the message to the receiver's mailbox (a buffer that holds messages that are sent to an actor)
  playerProfile ! PlayerProfile.LogName
  playerProfile ! PlayerProfile.UpdateName("john")
  playerProfile ! PlayerProfile.LogName
}

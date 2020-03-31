package com.evolutiongaming.bootcamp.akka_persistence

import akka.actor.Actor

class SimpleActor extends Actor {
  override def receive: Receive = ???
}

object SimpleActor {
  sealed trait Command
  case object ShowState extends Command
  case object Restart extends Command
  case class Add(entity: Int) extends Command

  case class State(
    entity: Int = 0
  ) {
    def plus(
      entity: Int
    ): State =
      copy(this.entity + entity)
  }
}
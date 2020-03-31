package com.evolutiongaming.bootcamp.akka_persistence

import akka.actor.{Actor, ActorRef, Stash}

class SimplePersistenceStashActor extends Actor with Stash {
  override def receive: Receive = ???
}

object SimplePersistenceStashActor {
  sealed trait Command
  case class Init(actorRef: ActorRef) extends Command
  case class Event(entity: Any) extends Command
  case class Recover(actor: ActorRef) extends Command
  private case object SwitchContext extends Command
  case object RecoverCompleted extends Command
}

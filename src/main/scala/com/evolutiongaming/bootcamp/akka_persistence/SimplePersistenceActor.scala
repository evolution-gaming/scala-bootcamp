package com.evolutiongaming.bootcamp.akka_persistence

import akka.actor.SupervisorStrategy.{Escalate, Resume, Stop}
import akka.actor.{OneForOneStrategy, SupervisorStrategy}
import akka.persistence.PersistentActor
import SimpleActor.State
import SimplePersistenceActor.{Add, Restart, ShowState}

import scala.concurrent.duration._
import scala.language.postfixOps

class SimplePersistenceActor extends PersistentActor {
  override def persistenceId: String = "simple_persistence_actor_1"

  override def supervisorStrategy: SupervisorStrategy = {
    OneForOneStrategy(maxNrOfRetries = 5, withinTimeRange = 10 seconds) {
      case _: ArithmeticException      => Resume
      case _: NullPointerException     => SupervisorStrategy.Restart
      case _: IllegalArgumentException => Stop
      case _: Exception                => Escalate
    }
  }

  var state = State()

  override def receiveRecover: Receive = {
    case event: Add => updateState(event)
  }

  override def receiveCommand: Receive = {
    case event: Add => persist(event) { event =>
      updateState(event)
    }
    case ShowState  => println(state)
    case Restart    => throw new NullPointerException
  }

  private def updateState(event: Add): Unit =
    state = state.plus(event.v)
}

object SimplePersistenceActor {
  sealed trait Command
  case object ShowState extends Command
  case object Restart extends Command
  case class Add(v: Int) extends Command

  case class State(entity: Int = 0) {
    def plus(entity: Int): State =
      copy(this.entity + entity)
  }
}

package com.evolutiongaming.bootcamp.akka_persistence

import akka.actor.SupervisorStrategy.{Escalate, Resume, Stop}
import akka.actor.{Actor, OneForOneStrategy, SupervisorStrategy}
import SimpleActor.{Add, Restart, ShowState, State}

import scala.concurrent.duration._
import scala.language.postfixOps

class SimpleActorImplementation extends Actor {

  var state = State()

  override def supervisorStrategy: SupervisorStrategy = {
    OneForOneStrategy(maxNrOfRetries = 5, withinTimeRange = 10 seconds) {
      case _: ArithmeticException      => Resume
      case _: NullPointerException     => SupervisorStrategy.Restart
      case _: IllegalArgumentException => Stop
      case _: Exception                => Escalate
    }
  }

  override def receive: Receive = {
    case ShowState => println(state)
    case Add(v)    => state = state.plus(v)
    case Restart   => throw new NullPointerException
    case _         =>
  }
}

object SimpleActorImplementation {
  sealed trait Command
  case object ShowState extends Command
  case object Restart extends Command
  case class Add(v: Int) extends Command

  case class State(entity: Int = 0) {
    def plus(entity: Int): State =
      copy(this.entity + entity)
  }
}

package com.evolutiongaming.bootcamp.akka.actors

import akka.actor.{Actor, FSM}

object StatePatterns extends App {

  // commands
  final case class Increment(value: Int)
  final case object Reset

  // 1. Mutable state pattern
  class Counter1 extends Actor {
    var count = 0

    def receive: Receive = {
      case Increment(value) =>
        count += value
      case Reset =>
        count = 0
    }
  }

  // 2. Behavior switch pattern
  class Counter2 extends Actor {
    // ~ a tail-recursive function, but it is asynchronous
    // state change is explicit and state is scoped to current behavior
    // we can change actor behavior for the next message
    def behavior(count: Int): Receive = {
      case Increment(value) =>
        // stay
        context.become(
          behavior(count + value)
        )
      case Reset =>
        context.become(init)
    }

    def init: Receive = behavior(0)

    override def receive: Receive =
      init
  }

  // 3. FSM (Finite State Machine) pattern
  // transition between a finite set of states based on incoming messages
  sealed trait State
  final case object Idle extends State
  final case object Active extends State

  sealed trait Data
  final case object Empty extends Data
  final case class CounterData(count: Int) extends Data

  class Counter3 extends FSM[State, Data] {
    startWith(Idle, Empty)

    when(Idle) {
      case Event(Increment(value), Empty) =>
        goto(Active) using CounterData(value)
    }

    when(Active) {
      case Event(Increment(value), CounterData(count)) =>
        stay using CounterData(count + value)

      case Event(Reset, _) =>
        goto(Idle) using Empty
    }

    onTransition {
      case Active -> Idle =>
        println("Counter reset")
    }

    initialize()
  }

  // 4. Event sourcing and CQRS
  // events are stored instead of current state
  // will be covered in the next lecture
}

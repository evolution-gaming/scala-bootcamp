package com.evolutiongaming.bootcamp.akka_persistence_2

import akka.actor.{ActorSystem, PoisonPill, Props}
import akka.persistence.PersistentActor

import scala.io.StdIn

// Storage backends for journals and snapshot stores are pluggable in the Akka persistence extension.

object Example1 extends App {
  // PersistentActor is a persistent, stateful actor.
  // It is able to persist events to a journal and can react to them in a thread-safe manner
  // By default, a persistent actor is automatically recovered on start and on restart by replaying journaled messages.
  class EmployeeShoppingBasketActor extends PersistentActor {

    // receiveRecover defines how state is updated during recovery by handling events messages
    // New messages can be sent to a persistent actor during recovery but these do not interfere with replayed messages.
    // They are stashed and received by a persistent actor after recovery phase completes.
    // The persistent actor will receive a special RecoveryCompleted message right after recovery and before any other received messages.
    // The actor will always receive a RecoveryCompleted message, even if there are no events in the journal and the snapshot store is empty, or if it’s a new persistent actor with a previously unused persistenceId.
    // If there is a problem with recovering the state of the actor from the journal,
    // onRecoveryFailure is called (logging the error by default) and the actor will be stopped.
    override def receiveRecover: Receive = { case event =>
      println(s"Received $event")
    }

    // def receive = receiveCommand in PersistentActor
    // a command is handled by validating it and generating an event which is then persisted and handled
    override def receiveCommand: Receive = { case message =>
      // The persist/persistAll method persists events asynchronously
      // and the event handler is executed for successfully persisted events
      // When persisting events with persist it is guaranteed that the persistent actor will not receive further commands
      // between the persist call and the execution(s) of the associated event handler.
      // Incoming messages are stashed until the persist is completed.
      // If persistence of an event fails, onPersistFailure will be invoked (logging the error by default), and the actor will unconditionally be stopped.
      // If persistence of an event is rejected before it is stored, e.g. due to serialization error,
      // onPersistRejected will be invoked (logging a warning by default) and the actor continues with the next message.
      persist(message) { e =>
        // Successfully persisted events are internally sent back to the persistent actor as individual messages
        // that trigger event handler executions
        // An event handler may close over persistent actor state and mutate it (not shown).
        // The sender of a persisted event is the sender of the corresponding command -> can reply to the sender of a command (not shown).
        println(s"Stored event $e")
      }
    }

    // A persistent actor must have an identifier.
    // persistenceId must be unique to a given entity in the journal (database table/keyspace).
    // When replaying messages persisted to the journal, you query messages with a persistenceId.
    // So, if two different entities share the same persistenceId, message-replaying behavior is corrupted.
    override def persistenceId: String = "user-11"

  }

  val system = ActorSystem("AkkaPersistenceSystem")
  val basket = system.actorOf(Props[EmployeeShoppingBasketActor]())

  StdIn.readLine()

  basket ! "banana"
  basket ! "apple"

  StdIn.readLine()

  // This can be dangerous when used with PersistentActor
  // (!) Actor may receive and (auto)handle the PoisonPill before it processes the other messages which have been put into its stash
  basket ! PoisonPill

  val basket2 = system.actorOf(Props[EmployeeShoppingBasketActor]())
  basket2 ! "orange"

  StdIn.readLine()

}

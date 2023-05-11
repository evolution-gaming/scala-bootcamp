package com.evolutiongaming.bootcamp.akka.actors

import akka.actor.{Actor, ActorRef, PoisonPill, Props}

// ActorContext is a fundamental abstraction in the Akka actor model
// that provides access to actor execution context and additional functionality:
// communication with other actors, scheduling, and managing the actor's lifecycle.
object ActorContext101 {

  class MyActor extends Actor {

    // 1. `self` returns a reference to the actor itself
    val selfRef: ActorRef = context.self
    self.path.name

    // actors can be organized into a hierarchy to manage complexity in the system.
    // the parent-child relationship is used to create an actor hierarchy.

    // 2. `actorOf` creates an actor as a child of current context
    val props: Props       = ???
    val childRef: ActorRef = context.actorOf(props, "my-child")

    // 3. `parent` returns a reference to the actor's parent
    context.parent ! "notify"

    // 4. `children` returns a collection of the actor's children,
    //    `child(name)` returns a child with the given name if it exists
    context.children.foreach(_ ! "notify")
    context.child("my-child").foreach(_ ! "notify")

    // note: name must be unique

    def receive: Receive = {
      case "cmd1" =>
        // 5. `become` replaces the current receive method with the specified behavior.
        // see [[StatePatterns]]
        val empty: Receive = { case _ =>
        }
        context.become(empty)
        context.become({
          case "cmd1" => ???
          case "cmd2" => ???
        })

      case "cmd2" =>
        // 6. `sender` returns the sender 'ActorRef' of the current message.
        val ref: ActorRef = context.sender()
        sender() ! "response"

      case "cmd3" =>
        // 7. `stop` stops the actor pointed to by the given ref
        context.stop(childRef)
        context.stop(self)

        // another way to stop
        // note: like sending other messages, it's an asynchronous operation
        childRef ! PoisonPill
        self ! PoisonPill

      // self learning - graceful stop
    }
  }

}

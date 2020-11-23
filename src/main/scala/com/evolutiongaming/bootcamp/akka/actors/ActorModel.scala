package com.evolutiongaming.bootcamp.akka.actors

import akka.actor.{Actor, ActorRef, ActorSystem, PoisonPill, Props}

// Actor Model
// actors represent objects and the actor model describes how these objects interact
// one of very fundamental part of actors is that they rely only on messages

// An Actor:
// - object with identity (a name)
// - has behavior (that can be changed)
// - only interact using async messages (~ human communication via emails)
object ActorModel extends App {

  // akka classic (untyped)

  // 1. describe your actor
  // Actor: trait
  final class EvoActor extends Actor {
    import EvoActor._
    // internal state, can be shared only via messages
    private var emailCounter = 0

    // need to define only 1 method - describes behavior
    // type Receive = PartialFunction[Any, Unit]
    override def receive: Receive = {
      case Hi             =>
        println("hi there")
      case Email(subject) =>
        emailCounter += 1
        println(s"I got the email: $subject.")

      // to make it stateful we need pass answer back to sender
      case HowManyEmailsYouGot =>
        // sender: reference to the last message sender
        sender() ! EmailCount(emailCounter) // ~ sender.tell(EmailCount(emailCounter), self)

      case other          =>
        println(s"unexpected: $other")
    }
  }
  object EvoActor {
    // actor name, if we did not specify, actor system will generate name
    val Name = "evo-actor"

    // configuration object (basically we need actor type and deployment config)
    def props: Props = Props[EvoActor]()

    // another way to specify props
    // Props(classOf[T], constructor params*)
    def props2: Props = Props(classOf[EvoActor])

    // Props(new Actor) - should not be used within another actor
    // in companion object is ok
    def props3: Props = Props(new EvoActor)

    // untyped actor can receive Any message but it's better to have sealed messages family
    sealed trait In
    case object Hi extends In
    final case class Email(subject: String) extends In
    case object HowManyEmailsYouGot extends In

    sealed trait Out
    final case class EmailCount(x: Int) extends Out
  }


  // ~ActorApp
  final class EvoMainActor extends Actor {
    // 2. create an actor as a child of another actor
    // - context:  ~local API (provides self ref, sender ref, system, etc)
    // - actorOf:  create an actor as a child of current context
    // - ActorRef: immutable handle/remote controller to an actor (can be shared, does not change in case of
    //   actor failure, etc)
    private val evoActorRef: ActorRef = context.actorOf(EvoActor.props, EvoActor.Name)

    println(s"Actor path example: $evoActorRef)")

    // 3. send message to our actorRef
    // - tell or ! sends a one-way asynchronous specified message (fire-and-forget semantics)
    // - all messages go to actor mailbox, then processed one-by-one
    evoActorRef ! EvoActor.Hi                  // implicitly pass sender
    evoActorRef ! EvoActor.Email("free pizza") // sender = self

    // another way to specify msg and sender explicitly
    // sender reference will be available in the receiving actor
    evoActorRef.tell(msg = EvoActor.Email("payment"), sender = Actor.noSender)
    evoActorRef ! "boom"

    evoActorRef ! EvoActor.HowManyEmailsYouGot

    // code above ^ will be executed during EvoMainActor start

    override def receive: Receive = {
      case EvoActor.EmailCount(x) =>
        println(s"my child received $x emails, now plz die")
        evoActorRef ! PoisonPill
        context.stop(self)
    }
  }

  // 4. create actor system (basically we need a name and config)
  val evoActorSystem: ActorSystem = ActorSystem("evo-actor-system")
  val evoMainRef: ActorRef = evoActorSystem.actorOf(Props[EvoMainActor](), "main")

  // 5. stop your system
  // evoActorSystem.terminate()
}

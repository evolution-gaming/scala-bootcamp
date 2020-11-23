package com.evolutiongaming.bootcamp.akka.actors

import akka.actor.{Actor, ActorSystem, Props, Stash}

import scala.concurrent.Future

object LongInitializationPattern extends App {

  final class LongInit extends Actor with Stash {

    import context.dispatcher

    override def preStart(): Unit = {
      import akka.pattern.pipe
      longRunningComputations pipeTo self
    }

    private def longRunningComputations: Future[LongInit.In] =
      Future {
        // some db- or external service call
        Thread.sleep(2000)
        LongInit.InitialState
      }

    // wait for state, keep requests
    private def notInitialized: Receive = {
      case LongInit.CommonRequest(x) =>
        println(s"stash $x")
        // save messages for the future processing when initial state is received
        stash()
      case LongInit.InitialState     =>
        context.become(initialized)
        // now process all
        unstashAll()
    }

    // normal processing
    private def initialized: Receive = {
      case LongInit.CommonRequest(x) => println(s"received $x")
    }

    override def receive: Receive = notInitialized
  }

  object LongInit {
    sealed trait In
    case object InitialState extends In
    final case class CommonRequest(x: String) extends In
  }


  val evoActorSystem: ActorSystem = ActorSystem("evo-actor-system")
  val ref = evoActorSystem.actorOf(Props[LongInit]())

  ref ! LongInit.CommonRequest("a")
  ref ! LongInit.CommonRequest("b")
  ref ! LongInit.CommonRequest("c")
}

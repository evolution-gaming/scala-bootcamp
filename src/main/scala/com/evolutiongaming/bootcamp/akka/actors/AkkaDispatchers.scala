package com.evolutiongaming.bootcamp.akka.actors

import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

// dispatchers are responsible for executing actor tasks.
// akka comes with a default dispatcher that is used by all actors unless otherwise specified.
// it is possible to define custom dispatchers and configure them according to specific needs.
// custom dispatchers can be used to achieve better performance, optimize resource utilization, or enforce specific scheduling policies
object AkkaDispatchers extends App {
  class MyActor extends Actor {
    override def receive: Receive = ???
  }

  // 1. define in config
  object MyActor {
    def props: Props = Props(new MyActor).withDispatcher("dispatchers.custom-dispatcher")
  }

  // 2. define programmatically
  val config = ConfigFactory.parseString(
    """
    my-dispatcher {
      type = Dispatcher
      executor = "fork-join-executor"
      fork-join-executor {
        parallelism-min = 2
        parallelism-factor = 2.0
        parallelism-max = 10
      }
      throughput = 100
    }
  """)

  val system = ActorSystem("MyActorSystem")
  val myActor = system.actorOf(Props[MyActor]().withDispatcher("my-dispatcher"), name = "MyActor")
}

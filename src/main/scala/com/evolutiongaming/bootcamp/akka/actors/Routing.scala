package com.evolutiongaming.bootcamp.akka.actors

import akka.actor.{Actor, ActorPath, ActorRef, ActorSystem, Props}
import akka.routing.RoundRobinPool

// for run and load balancing for many instances of one actor
object Routing extends App {

  final class Worker extends Actor {
    import Worker._
    override def receive: Receive = {
      case Request(id) => sender() ! Answer(self.path, id)
    }
  }
  object Worker {
    final case class Request(id: Int)
    final case class Answer(path: ActorPath, id: Int)
  }

  final class Main extends Actor {
    import Main._
    // a router pool that uses round-robin to select a routee
    private val workerRouter: ActorRef = context.actorOf(
      Props[Worker]().withRouter(RoundRobinPool(5)),
      "worker-pool",
    )

    override def receive: Receive = {
      case RequestCount(x) =>
        (1 to x).foreach { id =>
          // go to one actor from pool
          workerRouter ! Worker.Request(id)
        }

      case Worker.Answer(path, id) =>
        println(s"id=$id processed by $path")
    }
  }
  object Main {
    final case class RequestCount(x: Int)
  }

  val evoActorSystem: ActorSystem = ActorSystem("evo-actor-system")
  evoActorSystem.actorOf(Props[Main](), "main") ! Main.RequestCount(100)
  // id=2 processed by akka://evo-actor-system/user/main/worker-pool/$b
  // id=1 processed by akka://evo-actor-system/user/main/worker-pool/$a
  // id=5 processed by akka://evo-actor-system/user/main/worker-pool/$e
  // id=3 processed by akka://evo-actor-system/user/main/worker-pool/$c
  // ...
}

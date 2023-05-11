package com.evolutiongaming.bootcamp.akka.actors

import akka.actor.{Actor, ActorPath, ActorRef, ActorSystem, Props}
import akka.routing.ConsistentHashingRouter.ConsistentHashable
import akka.routing.{ConsistentHashingPool, RandomPool, RoundRobinPool}

// to distribute work across multiple instances of one actor
object Routing extends App {

  final class Worker extends Actor {
    import Worker._
    override def receive: Receive = { case Request(id) =>
      sender() ! Answer(self.path, id)
    }
  }
  object Worker {
    final case class Request(id: Int) extends ConsistentHashable {
      override def consistentHashKey: Any = id % 3
    }
    final case class Answer(path: ActorPath, id: Int)
  }

  final class Main extends Actor {
    import Main._
    // Round-robin routing
    // messages are distributed in a round-robin fashion to a group of actors
    private val workerRouter: ActorRef = context.actorOf(
      Props[Worker]().withRouter(RoundRobinPool(10)), // or RoundRobinPool(5).props(Props[Worker]())
      "worker-pool",
    )

    // Random routing
    // messages are randomly distributed to a group of actors
    private val workerRouter2: ActorRef = context.actorOf(
      Props[Worker]().withRouter(RandomPool(10)), // or RandomPool(5).props(Props[Worker]())
      "worker-pool2",
    )

    // Consistent hashing routing
    // messages are distributed to actors based on a consistent hashing algorithm
    // each message is hashed and mapped to a specific actor, based on the hash value.
    // Message must be handled by hashMapping, or implement [[ConsistentHashable]] or be wrapped in [[ConsistentHashableEnvelope]]
    private val workerRouter3: ActorRef = context.actorOf(
      Props[Worker]().withRouter(
        ConsistentHashingPool(
          10
        )
      ),
      "worker-pool3",
    )

    override def receive: Receive = {
      case RequestCount(x) =>
        (1 to x).foreach { id =>
          // go to one actor from pool
          workerRouter3 ! Worker.Request(id)
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

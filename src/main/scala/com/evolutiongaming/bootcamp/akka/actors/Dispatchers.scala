package com.evolutiongaming.bootcamp.akka.actors

import akka.pattern.pipe
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.util.Timeout

import scala.concurrent.Future
import scala.concurrent.duration._

// interacting actors and futures
object Dispatchers extends App {

  final class Main extends Actor {
    private val cache = context.actorOf(
      Props(new SimpleCache(3.seconds)).withDispatcher("dispatchers.custom-dispatcher"),
      "cache"
    )

    import context.dispatcher
    private val cancellableTask =
      context.system.scheduler.scheduleAtFixedRate(
        initialDelay = 0.seconds,
        interval = 1.seconds,
        receiver = cache,
        message = SimpleCache.Get("1")
      )
    // cancellableTask.cancel()

    override def receive: Receive = {
      case in: SimpleCache.In   => cache forward in
      case out: SimpleCache.Out => println(s"$out")
    }
  }

  final class SimpleCache(cleanAfter: FiniteDuration) extends Actor {
    import SimpleCache._

    // all futures inside actor run in execution context
    // dispatcher is potentially shared among multiple actors
    import context.dispatcher

    private var cache: Map[String, String] = Map.empty

    // eg webserver call
    private def someComputations(key: String): Future[String] =
      Future {
        Thread.sleep(200)
        s"result for $key"
      }

    override def receive: Receive = {
      case Get(key) =>
        if (cache.contains(key)) {
          println(s"getting result for $key")
          sender() ! Answer(cache(key))
        } else {
          println(s"computing result for $key")

          // correct code
          val client = sender()
          val futureResult = someComputations(key)
            .map(result => Result(key, result, client))

          futureResult pipeTo self

          /*
          // bad code
          someComputations(key).map { result =>
            // access to cache should not happen outside the actor scope
            cache += key -> result
            // sender() inside of future may return unexpected result
            sender ! Answer(result)
          }

          Do not refer to actor state from async running code!
          */
        }

      case Result(key, result, client) =>
        cache += key -> result
        client ! Answer(result)

      case Clean =>
        println("clean")
        cache = Map.empty
    }

    context.system.scheduler.scheduleOnce(cleanAfter) {
      self ! Clean
    }
  }

  object SimpleCache {
    // messages
    sealed trait In
    final case class Get(key: String) extends In

    sealed trait Out
    final case class Answer(value: String) extends Out

    // internal
    final case class Result(key: String, result: String, client: ActorRef)
    case object Clean
  }

  val evoActorSystem: ActorSystem = ActorSystem("evo-actor-system")
  val mainActor: ActorRef         = evoActorSystem.actorOf(Props(classOf[Main]), "main")

  // Ask pattern
  // ask pattern - sometimes called anti-pattern if it used inside of actor
  implicit val timeout: Timeout = Timeout(1.second) // implicit for `?`
  import evoActorSystem.dispatcher // for collect
  import akka.pattern.ask  // provide ask support

  (mainActor ? SimpleCache.Get("2")) // returns Future[Any]
    .mapTo[SimpleCache.Out]          // if cannot be cast (message is unhandled) then future failed
    .collect {
      case SimpleCache.Answer(x) => println(s"ask answer is $x")
    }
}

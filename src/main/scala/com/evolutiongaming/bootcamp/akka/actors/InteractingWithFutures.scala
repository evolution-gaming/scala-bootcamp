package com.evolutiongaming.bootcamp.akka.actors

import akka.actor.{Actor, ActorRef}
import akka.pattern.pipe

import scala.concurrent.Future

object InteractingWithFutures extends App {
  final case class Get(key: Int)

  final case class Answer(value: String)

  // internal
  final case class Result(key: Int, result: String, sender: ActorRef)

  class SimpleCache extends Actor {

    // all futures inside actor run in execution context
    // dispatcher is potentially shared among multiple actors
    import context.dispatcher

    private var cache: Map[Int, String] = Map.empty

    // eg webserver call
    private def someComputations(key: Int): Future[String] =
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
          val originalSender = sender()
          val futureResult: Future[Result] = someComputations(key)
            .map((result: String) => Result(key, result, originalSender))

          futureResult pipeTo self // without blocking, the actor continues to process messages


          // bad code
//          someComputations(key).map { result =>
//            // access to cache should not happen outside the actor scope
//            cache += key -> result
//            // sender() inside of future may return unexpected result
//            sender ! Answer(result)
//          }

          /// do not refer to actor state from async running code!
          // */
        }

      case Result(key, result, originalSender) =>
        cache += key -> result
        originalSender ! Answer(result)
    }
  }
}

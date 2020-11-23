package com.evolutiongaming.bootcamp.akka.actors

import akka.actor._
import akka.util.Timeout

import scala.concurrent._
import scala.concurrent.duration._
import scala.util.control.NoStackTrace

object Exercise2 extends App {
  object AskPattern {
    implicit class RichActorRef(val inner: ActorRef) extends AnyVal {
      def ask(msg: Any)(implicit refFactory: ActorRefFactory, timeout: Timeout): Future[Any] =
        AskPattern.ask(inner, msg)

      def ?(msg: Any)(implicit refFactory: ActorRefFactory, timeout: Timeout): Future[Any] = ask(msg)
    }

    def ask(ref: ActorRef, msg: Any)(implicit refFactory: ActorRefFactory, timeout: Timeout): Future[Any] = {
      val promise = Promise[Any]()
      refFactory.actorOf(Props(new AskActor(ref, msg, timeout, promise)))
      promise.future
    }

    final case class TimeoutException(msg: Any, timeout: Timeout)
      extends RuntimeException(s"Ask timeout after $timeout on message $msg") with NoStackTrace

    /*
    Actor should implement ask pattern:
    - send msg to targetRef
    - wait for response timeout max
    - if a response (of type Any) received - complete the promise with it
    - if timeout expires - fail the promise with TimeoutException
    - stop (context.stop(self)) after either outcome

    Use context.setReceiveTimeout!
     */
    private class AskActor(
      targetRef: ActorRef,
      msg: Any,
      timeout: Timeout,
      promise: Promise[Any],
    ) extends Actor {

      override def receive: Receive = ???
    }
  }

  final class WorkerActor extends Actor {
    override def receive: Receive = {
      case "ping"    =>
        sender() ! "pong"
      case "timeout" => //do nothing
    }
  }

  import AskPattern._

  implicit val system: ActorSystem = ActorSystem("Exercise2")
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val timeout: Timeout = Timeout(2.seconds)

  val workerRef = system.actorOf(Props(new WorkerActor), "worker")

  (workerRef ? "ping").foreach(println)
  (workerRef ? "timeout").failed.foreach(println)
}

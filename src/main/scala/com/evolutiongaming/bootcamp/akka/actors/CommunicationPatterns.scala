package com.evolutiongaming.bootcamp.akka.actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.util.Timeout

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.DurationInt

object CommunicationPatterns {
  // 1. Tell
  // the "tell" pattern is also known as the "fire and forget" pattern.
  // it is used when a message needs to be sent from one actor to another,
  // but the sender does not need to wait for a response

  final case class Greeting(message: String, ref: ActorRef)
  final case class Greet(ref: ActorRef)

  class Greeter extends Actor {
    def receive: Receive = { case Greeting(message, ref) =>
      context.sender()
      println(s"Hello $message! Sender is $ref")
    }
  }

  class Sender extends Actor {
    def receive: Receive = { case Greet(ref) =>
      context.sender()

      ref.!(Greeting("Alice", self)) // the reference to the sender actor is automatically passed along with the message
      ref ! Greeting("Alice", self) // the reference to the sender actor is automatically passed along with the message
      ref.tell(Greeting("Bob", self), self) // another syntax
      ref.tell(Greeting("Charlie", self), Actor.noSender) // another syntax
    }
  }

  def tellDemo: Unit = {
    val system  = ActorSystem("HelloSystem")
    val greeter = system.actorOf(Props[Greeter](), "greeter")
    val sender  = system.actorOf(Props[Sender](), "sender")

    val x: Unit = sender ! Greet(greeter)

    system.terminate()
  }

  // 2. Ask
  // the "ask" pattern is used when the sender needs to receive a response from the receiver,
  // and the sender blocks until the response is received.
  // sometimes called anti-pattern if it used inside of actor:
  // can lead to deadlocks as it can cause a circular dependency between actors

  final case class Request(query: String)
  final case class Response(results: List[String])

  class SearchActor extends Actor {
    def receive: Receive = { case Request(query) =>
      // Perform search and send response
      val response = Response(List("result1", "result2", "result3"))

      // sender: reference to the last message sender
      sender() ! response
    }
  }

  def askDemo: Unit = {
    import akka.pattern.ask // provide `?` (ask) support

    implicit val timeout: Timeout = Timeout(1.second) // implicit for `?`

    val system      = ActorSystem("SearchSystem")
    val searchActor = system.actorOf(Props[SearchActor], "searchActor")
    val query       = "Akka actors"
    val future      = (searchActor ? Request(query)).mapTo[Response] // returns a `Future`

    val results = Await.result(future, timeout.duration)
    println(s"Results for '$query': $results")
    system.terminate()
  }

  // 3. Pipe
  // the "pipe" pattern is used to forward the result of a future to (another) actor for further processing.
  // with `pipeTo`, the response is automatically sent to the downstream actor,
  // allowing it to continue processing without any delays.

  case class ProcessData(data: String)

  class DataProcessor extends Actor {
    def receive: Receive = { case ProcessData(data) =>
      println(s"Processing data: $data")
    }
  }

  def pipeDemo: Unit = {
    import akka.pattern.pipe // provide pipe support

    val system        = ActorSystem("DataSystem")
    val dataProcessor = system.actorOf(Props[DataProcessor], "dataProcessor")

    import system.dispatcher

    val data   = "some data"
    val future: Future[String] = Future {
      data.toUpperCase
    }

    future.map(ProcessData).pipeTo(dataProcessor) // async
    system.terminate()
  }

  // 4. Forward
  // the "forward" pattern is used to forward a message from one actor to another,
  // while preserving the original sender.

  final case class Request1(query: String)

  // 'mediator'
  class SearchActor1 extends Actor {
    // Create an indexer actor and forward the request to it
    val indexer: ActorRef = context.actorOf(Props[Indexer], "indexer")

    def receive: Receive = { case request: Request1 =>
      println(s"Forwarding request to indexer: ${request.query}")
      indexer forward request
    }
  }

  class Indexer extends Actor {
    def receive: Receive = { case request: Request1 =>
      println(s"Processing request: ${request.query}")
      sender() ! s"Result for ${request.query}"
    }
  }

  def forwardDemo: Unit = {
    import akka.pattern.ask
    implicit val timeout: Timeout = Timeout(3.seconds)

    val system      = ActorSystem("SearchSystem")
    val searchActor = system.actorOf(Props[SearchActor1], "searchActor")
    val query       = "Akka actors"
    val future      = searchActor ? Request1(query)
    val result      = Await.result(future, timeout.duration).asInstanceOf[String]
    println(s"Search result for '$query': $result")
    system.terminate()
  }

  final def main(args: Array[String]): Unit = {
    //tellDemo
    //askDemo
    // pipeDemo
    forwardDemo
  }

  // Actor collaboration
  // messages delivery guarantees: at most once
  // to make reliable - each transfer/deposit/withdraw should have unique id, store completed actions ids

  // message ordering:
  // - to the same destination order wil be preserved
  //     if  A.tell(B, m1) and A.tell(B, m2)  :  B will receive m1, m2 in order (akka specific)
  // - to the different destination: unknown (completely concurrently)
  //     if  A.tell(B, m1) and A.tell(C, m2)  :  order undefined here
}

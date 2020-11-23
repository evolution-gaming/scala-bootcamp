package com.evolutiongaming.bootcamp.akka.actors

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}

object Exercise1 extends App {
  object Protocol {
    final case class Task(id: Int)
    final case class Tasks(tasks: Vector[Task])
  }

  /*
  Receives Task, sends out Tasks to sinkRef.
  Should send tasks downstream batched by batchSize
   */
  final class BatcherActor(
    batchSize: Int,
    sinkRef: ActorRef,
  ) extends Actor {
    import Protocol._

    require(batchSize > 0)

    //    private val buffer: ArrayBuffer[Task] = ArrayBuffer.empty
    //    or
    //    private var buffer: Vector[Task] = Vector.empty

    override def receive: Receive = {
      case task: Task =>
        ???
    }
  }

  final class WorkerActor extends Actor with ActorLogging {
    import Protocol._

    override def receive: Receive = {
      case Tasks(tasks) =>
        log.info("Tasks received: {}", tasks)
    }
  }

  val system = ActorSystem("Exercise1")

  val workerRef = system.actorOf(Props(new WorkerActor), "worker")
  val batcherRef = system.actorOf(
    Props(new BatcherActor(
      batchSize = 2,
      sinkRef = workerRef,
    )),
    "batcher",
  )

  (1.to(7)).map(Protocol.Task).foreach(batcherRef ! _)
}

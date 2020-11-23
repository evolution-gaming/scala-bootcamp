package com.evolutiongaming.bootcamp.akka.actors

import akka.actor.{Actor, ActorRef, ActorSystem, OneForOneStrategy, Props, SupervisorStrategy, Terminated}

import scala.concurrent.duration._

object LifecycleAndSupervision extends App {

  final class Worker extends Actor {
    import Worker._

    // Hooks
    override def preStart(): Unit = {
      println("pre start")
    }
    override def postStop(): Unit = {
      println("post stop")
    }
    override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
      println(s"pre restart reason: ${ reason.getMessage }")
    }
    override def postRestart(reason: Throwable): Unit = {
      println("post restart")
    }

    override def receive: Receive = {
      case RestartCommand  => 1 / 0 // expect ArithmeticException
      case StopCommand     => Some(null).map(_.toString) // expect NullPointerException
      case ResumeCommand   => Map.empty.apply("") // expect NoSuchElementException
      case EscalateCommand => throw new RuntimeException("why not?")
    }
  }

  object Worker {
    // 'strategy will be applied'

    sealed trait Command
    case object RestartCommand
    case object StopCommand
    case object EscalateCommand
    case object ResumeCommand
  }


  final class SupervisorActor extends Actor {
    // when supervisorStrategy is not specified, actor uses SupervisorStrategy.defaultDecider
    override val supervisorStrategy: SupervisorStrategy =
    // one for one - only for one failed child actor
      OneForOneStrategy(maxNrOfRetries = 2, withinTimeRange = 1.second) {
        case _: NoSuchElementException =>
          println("worker resumes")
          SupervisorStrategy.Resume
        case _: ArithmeticException    =>
          println("worker restarts")
          SupervisorStrategy.Restart
        case _: NullPointerException   =>
          println("worker stops")
          SupervisorStrategy.Stop
        case _: Exception              =>
          println("error escalates") // will be applied `Restart` on higher level
          SupervisorStrategy.Escalate
      }

    // child
    private val worker: ActorRef = context.actorOf(props = Props[Worker](), name = "worker")
    // death watch
    context.watch(worker)

    override def receive: Receive = {
      case Terminated(`worker`) => println("worker terminated")
      case command              => worker forward command
    }
  }

  // playground
  val evoActorSystem: ActorSystem = ActorSystem("evo-actor-system")
  val supervisorActor: ActorRef = evoActorSystem.actorOf(Props[SupervisorActor]())
  import Worker._

  // 1
  // supervisorActor ! ResumeCommand
  // pre start
  // - exception time: decision resume
  // worker resume

  // 2
  // supervisorActor ! StopCommand
  // pre start
  // worker stop
  // - exception time: decision stop
  // post stop

  // 3
  supervisorActor ! RestartCommand
  // pre start
  // - exception time: decision restart, remains 1 attempt
  // worker restart
  // pre restart reason: / by zero
  // post restart
  supervisorActor ! RestartCommand
  // - exception time: decision restart, remains 0 attempt
  // worker restart
  // pre restart reason: / by zero
  // post restart
  supervisorActor ! RestartCommand
  // - exception time: decision stop
  // post stop
  // worker terminated
  supervisorActor ! RestartCommand
  // dead letters encountered

  // 4
  // supervisorActor ! EscalateCommand
  // pre start
  // - exception time: decision restart
  // worker escalate
  // post stop
  // pre start


  import evoActorSystem.dispatcher
  // evoActorSystem.scheduler.scheduleOnce(10.seconds) { evoActorSystem.terminate() }

}

package com.evolutiongaming.bootcamp.akka.actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.event.LoggingReceive

object MessageProcessing extends App {
  // every actor knows its own address (via self from Actor trait)
  // only messages can be sent to known address (ActorRef)
  // creating actor return its address
  // address can be sent via messages (to another actor or to self)

  // actors are completely independently executed
  // actors run fully concurrently (there is no notion of a global synchronization)
  // actors are completely encapsulated and isolated from each other (share nothing, no direct access is possible)

  // an actor is effectively single-threaded:
  // it will receive its messages, one after the other
  // for each message, it will invoke its behavior, possibly change the behavior for the next message and move on
  // processing one message is the atomic unit of execution
  // blocking is replaced by simply enqueueing messages, for later execution.


  object BankAccount {
    final case class Deposit(x: Int)
    final case class Withdraw(x: Int)

    case object Done
    case object Failure
  }

  final class BankAccount extends Actor {
    import BankAccount._

    override def receive: Receive = bankAccount(balance = 0)

    private def bankAccount(balance: Int): Receive = LoggingReceive {
      case Deposit(x) =>
        context.become(bankAccount(balance + x))
        sender() ! Done
      case Withdraw(x) if x <= balance =>
        context.become(bankAccount(balance - x))
        sender() ! Done
      case _ =>
        sender() ! Failure
    }
  }

  object WireTransfer {
    final case class Transfer(from: ActorRef, to: ActorRef, amount: Int)
    case object Done
    case object Failure
  }

  final class WireTransfer extends Actor {
    import WireTransfer._

    override def receive: Receive = LoggingReceive {
      case Transfer(from, to, amount) =>
        // 1. need to get amount from
        from ! BankAccount.Withdraw(amount)
        context.become(awaitWithdraw(to, amount, sender()))
    }

    private def awaitWithdraw(to: ActorRef, amount: Int, client: ActorRef): Receive = LoggingReceive {
      case BankAccount.Done =>
        // 2. make deposit to
        to ! BankAccount.Deposit(amount)
        context.become(awaitDeposit(client))
      case BankAccount.Failure =>
        client ! Failure
        context.stop(self)
    }

    private def awaitDeposit(client: ActorRef): Receive = LoggingReceive {
      case BankAccount.Done =>
        client ! Done
        context.stop(self)
    }
  }

  final class TransferMain extends Actor {
    private def spawnAccount(name: String): ActorRef = context.actorOf(Props[BankAccount](), name)

    private val accA = spawnAccount("acc-A")
    private val accB = spawnAccount("acc-B")

    // make positive balance for one account
    accA ! BankAccount.Deposit(100)

    override def receive: Receive = LoggingReceive {
      case BankAccount.Done => makeTransfer(50)
    }

    private def makeTransfer(x: Int): Unit = {
      val transfer = context.actorOf(Props[WireTransfer](), "transfer")
      transfer ! WireTransfer.Transfer(from = accA, to = accB, amount = x)

      context.become {
        case WireTransfer.Done =>
        println("done")
        context.stop(self)
      }
    }
  }

  val evoActorSystem: ActorSystem = ActorSystem("evo-actor-system")
  evoActorSystem.actorOf(Props[TransferMain](), "main")

  // Actor collaboration
  // messages delivery guarantees: at most once
  // to make reliable - each transfer/deposit/withdraw should have unique id, store completed actions ids

  // message ordering:
  // - to the same destination order wil be preserved
  //     if  A.tell(B, m1) and A.tell(B, m2)  :  B will receive m1, m2 in order (akka specific)
  // - to the different destination: unknown (completely concurrently)
  //     if  A.tell(B, m1) and A.tell(C, m1)  :  order undefined here
}

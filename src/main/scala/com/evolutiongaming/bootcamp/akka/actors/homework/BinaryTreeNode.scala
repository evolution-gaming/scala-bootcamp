package com.evolutiongaming.bootcamp.akka.actors.homework

import akka.actor.{Actor, ActorRef, Props}

object BinaryTreeNode {
  private sealed trait Position

  private case object Left extends Position
  private case object Right extends Position

  def props(elem: Int, initiallyRemoved: Boolean): Props = Props(new BinaryTreeNode(elem, initiallyRemoved))
}

final class BinaryTreeNode(val elem: Int, initiallyRemoved: Boolean) extends Actor {
  import BinaryTreeNode._
  import BinaryTreeSet.Operation._
  import BinaryTreeSet.OperationReply._

  private var subtrees = Map[Position, ActorRef]()
  private var removed = initiallyRemoved

  override def receive: Receive = {
    case _ => ???
  }

  private def doInsert(m: Insert): Unit = {
    ???
  }

  private def doContains(m: Contains): Unit = {
    ???
  }

  private def doRemove(m: Remove): Unit = {
    ???
  }
}

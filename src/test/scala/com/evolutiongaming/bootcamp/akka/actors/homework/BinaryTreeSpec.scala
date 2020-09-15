package com.evolutiongaming.bootcamp.akka.actors.homework

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.evolutiongaming.bootcamp.akka.actors.homework.BinaryTreeSet.Operation._
import com.evolutiongaming.bootcamp.akka.actors.homework.BinaryTreeSet.OperationReply._
import com.evolutiongaming.bootcamp.akka.actors.homework.BinaryTreeSet.{
  Operation,
  OperationReply
}
import org.scalatest.flatspec.AnyFlatSpec

import scala.concurrent.duration._

class BinaryTreeSpec extends AnyFlatSpec {

  "BinaryTreeSpec" should "be correct" in new Scope {
    correctReplies()
  }

  "BinaryTreeSpec" should "inserts and lookup" in new Scope {
    insertsAndSearch()
  }

  class Scope
      extends TestKit(ActorSystem("BinaryTreeSuite"))
      with ImplicitSender {
    def correctReplies(): Unit = {
      val requester = TestProbe()
      val requesterRef = requester.ref
      val ops = List(
        Insert(requesterRef, id = 100, 1),
        Contains(requesterRef, id = 50, 2),
        Remove(requesterRef, id = 10, 1),
        Insert(requesterRef, id = 20, 2),
        Contains(requesterRef, id = 80, 1),
        Contains(requesterRef, id = 70, 2)
      )

      val expectedReplies = List(
        OperationFinished(id = 10),
        OperationFinished(id = 20),
        ContainsResult(id = 50, false),
        ContainsResult(id = 70, true),
        ContainsResult(id = 80, false),
        OperationFinished(id = 100)
      )

      verify(requester, ops, expectedReplies)
    }

    def insertsAndSearch(): Unit = {
      val topNode = system.actorOf(Props[BinaryTreeSet]())

      topNode ! Contains(testActor, id = 1, 1)
      expectMsg(ContainsResult(1, false))

      topNode ! Insert(testActor, id = 2, 1)
      topNode ! Contains(testActor, id = 3, 1)

      expectMsg(OperationFinished(2))
      expectMsg(ContainsResult(3, true))
      ()
    }

    def verify(probe: TestProbe,
               ops: Seq[Operation],
               expected: Seq[OperationReply]): Unit = {
      val topNode = system.actorOf(Props[BinaryTreeSet]())

      ops foreach { op =>
        topNode ! op
      }

      receiveN(probe, ops, expected)
    }

    def receiveN(requester: TestProbe,
                 ops: Seq[Operation],
                 expectedReplies: Seq[OperationReply]): Unit =
      requester.within(5.seconds) {
        val repliesUnsorted = for (i <- 1 to ops.size)
          yield
            try {
              requester.expectMsgType[OperationReply]
            } catch {
              case ex: Throwable if ops.size > 10 =>
                sys.error(
                  s"failure to receive confirmation $i/${ops.size}\n$ex"
                )
              case ex: Throwable =>
                sys.error(
                  s"failure to receive confirmation $i/${ops.size}\nRequests:" + ops
                    .mkString("\n    ", "\n     ", "") + s"\n$ex"
                )
            }
        val replies = repliesUnsorted.sortBy(_.id)
        if (replies != expectedReplies) {
          val pairs = (replies zip expectedReplies).zipWithIndex filter (
            x => x._1._1 != x._1._2
          )
          fail(
            "unexpected replies:" + pairs
              .map(
                x => s"at index ${x._2}: got ${x._1._1}, expected ${x._1._2}"
              )
              .mkString("\n    ", "\n    ", "")
          )
        }
      }
  }
}

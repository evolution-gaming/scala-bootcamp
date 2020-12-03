package com.evolutiongaming.bootcamp.akka_persistence_2

import akka.actor.{ActorSystem, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings, ShardRegion}
import akka.persistence.{PersistentActor, RecoveryCompleted, SnapshotOffer}
import com.typesafe.config.ConfigFactory

import scala.io.StdIn

object Example5 extends App {

  // TODO remove this and use Example4
  class EmployeeShoppingBasketActor extends PersistentActor {

    private var basket = List.empty[String]

    override def preStart(): Unit = {
      super.preStart()
      println(s"Starting actor ${self.path.name}")
    }

    override def receiveRecover: Receive = {
      case SnapshotOffer(metadata, basketFromSnapshot: List[String]) =>
        basket = basketFromSnapshot

      case event =>
        println(s"Received $event")
    }

    override def receiveCommand: Receive = {
      case item: String =>

        persist(item) { e =>
          basket = item :: basket // storing item to basket
          println(s"Storing event $e")
          // As we only add items, in sake of simplicity, let's store snapshot each 5th item.
          if (basket.size % 5 == 0)
            saveSnapshot(basket)
        }
    }

    override def persistenceId: String = self.path.name

  }

  val system = ActorSystem("AkkaPersistenceSystem",
    ConfigFactory.parseString(
    """ |akka.actor.provider="akka.cluster.ClusterActorRefProvider"
        |
        |akka.persistence.journal.plugin = "akka.persistence.journal.leveldb"
        |akka.persistence.snapshot-store.plugin = "akka.persistence.snapshot-store.local"
        |
        |akka {
        |  cluster {
        |    seed-nodes = [
        |      "akka://AkkaPersistenceSystem@127.0.0.1:25520"
        |    ]
        |  }
        |}""".stripMargin)
  )

  def extractEntityId: ShardRegion.ExtractEntityId = {
    case cmd: String =>
      val splitterd = cmd.split(" ")
      splitterd(0) -> splitterd(1)
  }

  val extractShardId: ShardRegion.ExtractShardId = {
    case cmd: String => cmd.hashCode.toString
  }

  val shard = ClusterSharding(system).start(
    typeName = "BasketShard",
    entityProps = Props(new EmployeeShoppingBasketActor()),
    settings = ClusterShardingSettings(system),
    extractEntityId = extractEntityId,
    extractShardId = extractShardId
  )

  shard ! "1 apple"
  shard ! "2 banana"

  StdIn.readLine()

}

package com.evolutiongaming.bootcamp.akka_persistence_2

import akka.actor.{ActorSystem, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings, ShardRegion}
import akka.persistence.{PersistentActor, RecoveryCompleted, SnapshotOffer}
import com.typesafe.config.ConfigFactory

import scala.io.StdIn
import com.evolutiongaming.bootcamp.akka_persistence_2.Example4.EmployeeShoppingBasketActor

object Example5 extends App {

  val system = ActorSystem(
    "AkkaPersistenceSystem",
    ConfigFactory.parseString(""" |akka.actor.provider="akka.cluster.ClusterActorRefProvider"
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
                                |}""".stripMargin),
  )

  def extractEntityId: ShardRegion.ExtractEntityId = { case cmd: AddItem =>
    cmd.enityId -> cmd
  }

  val maxNodes = 10

  val extractShardId: ShardRegion.ExtractShardId = { case cmd: AddItem =>
    (cmd.enityId.hashCode % maxNodes).toString
  }

  val shard = ClusterSharding(system).start(
    typeName = "BasketShard",
    entityProps = Props(new EmployeeShoppingBasketActor()),
    settings = ClusterShardingSettings(system),
    extractEntityId = extractEntityId,
    extractShardId = extractShardId,
  )

  shard ! AddItem("entity_id_1", "apple")
  shard ! AddItem("entity_id_1", "orange")

  shard ! AddItem("entity_id_2", "apple")

  StdIn.readLine()

}

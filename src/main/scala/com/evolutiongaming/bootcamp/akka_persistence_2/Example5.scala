package com.evolutiongaming.bootcamp.akka_persistence_2

import akka.actor.{ActorSystem, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings, ShardRegion}
import com.evolutiongaming.bootcamp.akka_persistence_2.Example4.EmployeeShoppingBasketActor
import com.typesafe.config.ConfigFactory

import scala.io.StdIn

object Example5 extends App {

  val system = ActorSystem(
    "AkkaPersistenceSystem",
    ConfigFactory.parseString(""" |akka.actor.provider="akka.cluster.ClusterActorRefProvider"
                                |
                                |akka.persistence.journal.plugin = "akka.persistence.journal.inmem"
                                |akka.persistence.snapshot-store.plugin = "akka.persistence.snapshot-store.local"
                                |
                                |akka {
                                |  cluster {
                                |    seed-nodes = [
                                |      "akka://AkkaPersistenceSystem@127.0.0.1:25520"
                                |    ]
                                |    shutdown-after-unsuccessful-join-seed-nodes = 10s
                                |    coordinated-shutdown.exit-jvm = on
                                |  }
                                |  
                                |akka.actor.allow-java-serialization = on
                                |akka.actor.warn-about-java-serializer-usage = off
                                |}""".stripMargin),
  )

  // Cluster sharding is typically used when you have many stateful actors
  // that together consume more resources (e.g. memory) than fit on one machine.
  // You can distribute actors across several nodes in the cluster
  // and want to be able to interact with them using their logical identifier,
  // but without having to care about their physical location in the cluster,
  // which might also change over time, for instance during rebalancing

  // The ShardRegion actor is started on each node in the cluster, or group of nodes tagged with a specific role.
  // A shard is a group of entities that will be managed together.
  // The grouping is defined by the extractShardId function shown below.
  // For a specific entity identifier the shard identifier must always be the same.
  // Otherwise the entity actor might accidentally be started in several places at the same time.

  // Messages to the entities are always sent via the local ShardRegion.
  // The ShardRegion will lookup the location of the shard for the entity if it does not already know its location.
  // The ShardCoordinator decides which ShardRegion shall own the Shard and informs that ShardRegion
  // The region will confirm this request and create the Shard supervisor as a child actor.
  // The individual Entities will then be created when needed by the Shard actor.
  // Incoming messages thus travel via the ShardRegion and the Shard to the target Entity.
  // Local SR -> SC -> Local SR / Remote SR -> Shard -> Entity

  def extractEntityId: ShardRegion.ExtractEntityId = { case cmd: AddItem =>
    cmd.entityId -> cmd
  }

  val maxNodes = 10

  val extractShardId: ShardRegion.ExtractShardId = { case cmd: AddItem =>
    (cmd.entityId.hashCode % maxNodes).toString
  }

  // Register a named entity type by defining the Props of the entity actor
  // and functions to extract entity and shard identifier from messages
  val shard = ClusterSharding(system).start(
    typeName = "BasketShard",
    entityProps = Props(new EmployeeShoppingBasketActor()),
    settings = ClusterShardingSettings(system),
    extractEntityId = extractEntityId,
    extractShardId = extractShardId,
  ) // returns The ShardRegion actor reference for a named entity type

  shard ! AddItem("entity_id_1", "apple")
  shard ! AddItem("entity_id_1", "orange")

  shard ! AddItem("entity_id_2", "apple")

  StdIn.readLine()

}

// A cluster is made up of a set of member nodes.
// The identifier for each node is a hostname:port:uid tuple.
// A node could be a member of a cluster without hosting any actors.
// Joining a cluster is initiated by issuing a Join command to one of the nodes in the cluster to join.
//
// Member states:
// joining - transient state when joining a cluster
// weakly up - transient state while network split
// up - normal operating state
// preparing for shutdown / ready for shutdown - an optional state that can be moved to before doing a full cluster shut down
// leaving / exiting - states during graceful removal
// down - marked as down (no longer part of cluster decisions)
// removed - tombstone state (no longer a member)
//
// The purpose of the leader is to confirm state changes when convergence is reached.
// The leader can be determined by each node unambiguously after gossip convergence.
// Any node might be required to take the role of the leader depending on the current cluster composition.
//
// If a node is unreachable then gossip convergence is not possible and therefore most leader actions are impossible.
//
// User actions
// join - join a single node to a cluster
// leave - tell a node to leave the cluster gracefully, normally triggered by ActorSystem or JVM shutdown through coordinated shutdown
// down - mark a node as down. This action is required to remove crashed nodes (that did not ‘leave’) from the cluster.
//         It can be triggered manually, through Cluster HTTP Management, or automatically by a downing provider like Split Brain Resolver
//
// Leader Actions
// The leader has the duty of confirming user actions to shift members in and out of the cluster:
// joining ⭢ up
// joining ⭢ weakly up (no convergence is needed for this leader action to be performed which works even if there are unreachable nodes)
// weakly up ⭢ up (after full convergence is reached again)
// leaving ⭢ exiting
//  exiting ⭢ removed
//  down ⭢ removed

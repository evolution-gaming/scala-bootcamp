package com.evolution.services

import akka.actor.ActorSystem
import com.evolution.clients.CasinoClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class ScheduledUpdates(
    client: CasinoClient,
    casinoService: CasinoService,
    system: ActorSystem
) {
  def start(): Unit =
    system.scheduler.schedule(initialDelay = 1.second, interval = 5.seconds)(
      casinoService.saveAll(client.retrieveCasinos)
    )
}

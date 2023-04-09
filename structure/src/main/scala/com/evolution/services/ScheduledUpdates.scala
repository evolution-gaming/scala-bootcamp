package com.evolution.services

import akka.actor.{ActorSystem, Cancellable}
import com.evolution.clients.AppHttpClient
import com.evolution.infrastructure.utils.Utils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class ScheduledUpdates(
  client: AppHttpClient,
  casinoService: CasinoService,
  system: ActorSystem,
) {
  def start(): Unit = {
    val casinosRetrieval: Cancellable =
      system.scheduler.schedule(initialDelay = 1.second, interval = 5.seconds) {
        casinoService.saveAll(client.retrieveCasinos)
      }
    system.scheduler.scheduleOnce(25.seconds) {
      casinosRetrieval.cancel()
      Utils.logDebug("finished casinos retrieval!")
    }
  }
}

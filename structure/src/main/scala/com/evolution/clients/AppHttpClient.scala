package com.evolution.clients

import com.evolution.domain._
import com.evolution.infrastructure.http.{HttpClient, Request}
import com.evolution.infrastructure.json.JsonParser
import com.evolution.infrastructure.utils.Utils
import com.evolution.services.CasinoService

class AppHttpClient(
  service: CasinoService,
  http: HttpClient,
  conf: Config,
  jsonParser: JsonParser,
) {
  def retrieveCasinos = {
    val response = http.post(Request(service.lastUpdated.toString, conf.url))
    val casinos  = jsonParser.parseList(response.body)
    Utils.log(s"retrieved casinos: $casinos")
    casinos
  }
  def retrieveUsers   = Nil
}

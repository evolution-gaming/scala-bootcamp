package com.evolution.infrastructure.http

import scala.util.Random

final case class Request(body: String = "", url: String)
final case class Response(body: String = "")

class HttpClient {
  def get(request: Request): Response  = RandomResponse()
  def post(request: Request): Response = RandomResponse()
}

object RandomResponse {
  def apply(): Response = {
    val json =
      s"""{ "id": "id$rnd", "name": "Casino$rnd", "status": $status },"""
    Response(json.repeat(count).stripSuffix(",").prepended('[').appended(']'))
  }
  private def rnd       = Random.between(1000, 9999)
  private def status    = Random.between(0, 2)
  private def count     = Random.between(1, 5)
}

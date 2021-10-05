package com.evolution.infrastructure.http

trait Controller

class HttpServer(controllers: Controller*) {
  def start(): Unit = println(s"Server Started for $controllers")
}

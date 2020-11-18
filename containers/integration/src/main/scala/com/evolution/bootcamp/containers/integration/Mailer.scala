package com.evolution.bootcamp.containers.integration

import com.redis.RedisClient

object Mailer extends App {
  val ma = new MailAgent()
  val r = new RedisClient("localhost", 6379)
  val c = new MailCounter(r, "cnt")

  ma.sendExampleEmail("Hello from Scala app", "This is email from scala app.") match {
    case Left(error) => println(s"Error occurred: $error")
    case Right(_) =>
      c.addOne
      println("Email sent")
  }
}

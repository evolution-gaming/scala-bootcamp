package com.evolution.bootcamp.containers.integration

import com.redis.RedisClient

class MailCounter(redisClient: RedisClient, counterName: String) {

  def addOne: Long = {
    redisClient.incr(counterName)
  }.getOrElse(0)

}

package com.evolutiongaming.bootcamp.testing2.hal9000

object HAL9000 {

  private[hal9000] def twice(x: Int): Int = x * 2

  def letAustronautIn(): Unit = {
    throw new RuntimeException("I'm sorry Dave, I'm afraid I can't do that")
  }

}
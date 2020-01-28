package com.evolutiongaming.bootcamp.monitoring

import cats.effect.IO

object Random {
  def randomGaussian: IO[Double] = IO {
    scala.util.Random.nextGaussian()
  }

  def randomDouble: IO[Double] = IO {
    scala.util.Random.nextDouble()
  }
}

package com.evolutiongaming.bootcamp.monitoring

import cats.effect.{IO, Timer}
import scala.concurrent.duration._
import scala.util.Random

class Service(implicit timer: Timer[IO]) {
  private val random = new Random(0)
  private def randomGaussian: IO[Double] = IO(random.nextGaussian())
  private def randomDouble: IO[Double] = IO(random.nextDouble())

  def fixedDelay(milliseconds: Int): IO[Unit] = IO.sleep(milliseconds.milliseconds)

  def normalDistributionDelay(meanMilliseconds: Int, standardDeviation: Int): IO[Unit] = {
    for {
      g <- randomGaussian
      d = Math.max(meanMilliseconds + g * standardDeviation, 0)
      r <- IO.sleep(d.milliseconds)
    } yield r
  }

  def unreliable(threshold: Double): IO[Unit] = {
    for {
      r       <- randomDouble
      result  <- if (r <= threshold) IO.pure(())
                 else IO.raiseError(new RuntimeException("Unreliable service is unreliable"))
    } yield result
  }
}

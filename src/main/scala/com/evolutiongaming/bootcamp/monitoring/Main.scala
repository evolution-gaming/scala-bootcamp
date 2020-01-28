package com.evolutiongaming.bootcamp.monitoring

import cats.effect.{ExitCode, IO, IOApp}
import org.http4s.dsl.impl.Root
import org.http4s.server.blaze.BlazeServerBuilder
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.implicits._
import io.chrisdavenport.log4cats.Logger

import scala.concurrent.duration._
import Random._
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

// TODO: instructions to launch Prometheus server and Grafana in Docker
// TODO: Gatling tests to generate load
// TODO: exercises that require to add logs, metrics, check Grafana, etc.
object Main extends IOApp {
  private implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  private def okAfter(duration: FiniteDuration) = {
    Logger[IO].info(s"Sleeping $duration") *>
    IO.sleep(duration) *>
    Ok("OK")
  }

  private val service = HttpRoutes.of[IO] {
    case GET -> Root / "fixed-delay" / IntVar(milliseconds) =>
      okAfter(milliseconds.milliseconds)

    case GET -> Root / "normal-distribution-delay" / IntVar(meanMilliseconds) / IntVar(standardDeviation) =>
      for {
        g <- randomGaussian
        d = Math.max(meanMilliseconds + g * standardDeviation, 0)
        r <- okAfter(d.milliseconds)
      } yield r

    case GET -> Root / "unreliable" / IntVar(percentageSuccessful) =>
      val threshold = percentageSuccessful / 100.0
      for {
        r       <- randomDouble
        result  <- if (r <= threshold) Ok("OK") else InternalServerError("Error")
      } yield result

    case GET -> Root / "metrics" =>
      Ok("metrics") // TODO: implement Prometheus pull metrics end-point

  }.orNotFound

  def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO]
      .bindHttp(8080, "localhost")
      .withHttpApp(service)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
}

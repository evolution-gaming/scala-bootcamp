package com.evolutiongaming.bootcamp.monitoring

import cats.effect.{ExitCode, IO, IOApp}
import org.http4s.dsl.impl.Root
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.{HttpRoutes, Response}
import org.http4s.dsl.io._
import org.http4s.implicits._
import io.chrisdavenport.epimetheus._
import io.chrisdavenport.epimetheus.http4s.EpimetheusOps
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s.server.middleware.Metrics
import cats.implicits._

import scala.concurrent.ExecutionContext

/*
 * Follow ReadMe.md for instructions.
 *
 * You can run a simple load test using:
 * `ab -n 100 -c 10 http://127.0.0.1:9000/normal-distribution-delay/5000/1000`
 */
object Main extends IOApp {
  private def application(service: Service, logger: Logger[IO], collectorRegistry: CollectorRegistry[IO], requestsCounter: Counter[IO]) = {
    def asOk(f: IO[Unit]): IO[Response[IO]] = f >> Ok("OK")

    val default = HttpRoutes.of[IO] {
      case GET -> Root / "fixed-delay" / IntVar(milliseconds) =>
        for {
          _       <- requestsCounter.inc // Exercise. This is only an example for a counter. Remove, move or improve.
          result  <- asOk(service.fixedDelay(milliseconds))
        } yield result

      case GET -> Root / "normal-distribution-delay" / IntVar(meanMilliseconds) / IntVar(standardDeviation) =>
        for {
          _       <- requestsCounter.inc  // Exercise. This is only an example for a counter. Remove, move or improve.
          result  <- asOk(service.normalDistributionDelay(meanMilliseconds, standardDeviation))
        } yield result

      case GET -> Root / "unreliable" / IntVar(percentageSuccessful) =>
        asOk(service.unreliable(percentageSuccessful / 100.0))

      case GET -> Root / "metrics" =>
        for {
          _               <- logger.warn("Invoked") // Exercise. This is only an example for logging. Remove or improve.
          currentMetrics  <- collectorRegistry.write004
          response        <- Ok(currentMetrics)
        } yield response

    }

    default
  }

  def run(args: List[String]): IO[ExitCode] = {
    val logger = Slf4jLogger.getLogger[IO]
    for {
      _                 <- logger.info("Starting!!!") // Exercise. This is only an example for logging. Remove or improve.
      collectorRegistry <- CollectorRegistry.buildWithDefaults[IO]
      requestsCounter   <- Counter.noLabels(
                              collectorRegistry,
                              Name("requests"),
                              "Requests Counter",
                           )

      service           =  new Service
      routes            =  application(service, logger, collectorRegistry, requestsCounter)

      meteredRoutes     <- EpimetheusOps.server(collectorRegistry).map(metricOps => Metrics[IO](metricOps)(routes))

      _                 <- BlazeServerBuilder[IO](ExecutionContext.global)
        .bindHttp(9000, "0.0.0.0")
        .withHttpApp(meteredRoutes.orNotFound)
        .serve
        .compile
        .drain
    } yield ExitCode.Success
  }
}

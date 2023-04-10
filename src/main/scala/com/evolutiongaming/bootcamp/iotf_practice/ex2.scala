package com.evolutiongaming.bootcamp.iotf_practice

object ex2 {

  sealed trait State
  case object Open   extends State
  case object Closed extends State

  trait Metrics[F[_]] {
    def increase: F[Unit]
    def decrease: F[Unit]
    def gaugeTo(state: State): F[Unit]
  }

  object Metrics {
    /*
     * Task: implement, should use provider
     */
    def make[F[_]](provider: PrometheusProvider[F]): Metrics[F] = new Metrics[F] {
      def increase: F[Unit]              = ???
      def decrease: F[Unit]              = ???
      def gaugeTo(state: State): F[Unit] = ???
    }
  }

  /* Let's assume that it is a library code */
  trait PrometheusProvider[F[_]] {
    def getCounter: F[Int]
    def setCounter(i: Int): F[Unit]
    def setGauge(state: String): F[Unit]
  }

}

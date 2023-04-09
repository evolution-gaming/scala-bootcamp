package com.evolutiongaming.bootcamp.iotf_practice

object ex1 {

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
     * Task: implement, should do nothing
     */
    def empty[F[_]]: Metrics[F] = new Metrics[F] {
      def increase: F[Unit]              = ???
      def decrease: F[Unit]              = ???
      def gaugeTo(state: State): F[Unit] = ???
    }

    /*
     * Task: implement, should print to console "increase", "decrease", and s"gauge: $state"
     */
    def console[F[_]]: Metrics[F] = new Metrics[F] {
      def increase: F[Unit]              = ???
      def decrease: F[Unit]              = ???
      def gaugeTo(state: State): F[Unit] = ???
    }
  }

}

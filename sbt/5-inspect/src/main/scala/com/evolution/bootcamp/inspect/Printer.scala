package com.evolution.bootcamp.inspect

trait Printer[F[_]] {

  def print: F[Unit]

}
object Printer {

  def create[F[_]: Console]: Printer[F] = new Printer[F] {

    def print = Console[F].putStrLn("""
      The Hungry Young Camel

      Starving me from meal to meal,
      Don't I get a rotten deal?
      With an appetitie like mine
      What's two pailfuls at a time?

      Samuil Marshak
    """)

  }

}

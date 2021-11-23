package com.evolutiongaming.bootcamp.tf.practice.effects

import cats.effect.Sync

import java.util.Currency

trait CurrencySupport[F[_]] {
  def parse(str: String): F[Currency]
}

object CurrencySupport {

  def apply[F[_]: CurrencySupport]: CurrencySupport[F] = implicitly

  implicit class CurrencySupportStringOps[F[_]: CurrencySupport](str: String) {
    def toCurrency: F[Currency] = CurrencySupport[F].parse(str)
  }

  implicit def currencySupport[F[_]: Sync]: CurrencySupport[F] =
    (str: String) => Sync[F].delay(Currency.getInstance(str))
}

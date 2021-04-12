package com.evolutiongaming.bootcamp.tf.shopping.effects

import cats.effect.Sync

import java.util.Currency

trait CurrencySupport[F[_]] {
  def parse(code: String): F[Currency]
}

object CurrencySupport {

  def apply[F[_]: CurrencySupport]: CurrencySupport[F] = implicitly

  implicit class CurrencySupportStringOps[F[_]: CurrencySupport](str: String) {
    def toCurrency: F[Currency] = CurrencySupport[F].parse(str)
  }

  implicit def currencySupport[F[_]: Sync]: CurrencySupport[F] = (code: String) =>
    Sync[F].delay(Currency.getInstance(code))

}

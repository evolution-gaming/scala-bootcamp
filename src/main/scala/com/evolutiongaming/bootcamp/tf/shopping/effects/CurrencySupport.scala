package com.evolutiongaming.bootcamp.tf.shopping.effects

import java.util.Currency

trait CurrencySupport[F[_]] {
  def parse(code: String): F[Currency]
}

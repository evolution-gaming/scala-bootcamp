package com.evolutiongaming.bootcamp.tf.shopping.domain

import java.util.Currency

object money {

  final case class Money(amount: BigDecimal, currency: Currency)

}

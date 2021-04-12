package com.evolutiongaming.bootcamp.tf.shopping.domain

import io.circe._
import io.circe.generic.JsonCodec

import java.util.Currency
import scala.util.Try

object money {

  @JsonCodec
  final case class Money(amount: BigDecimal, currency: Currency)

  implicit val currencyDecoder: Decoder[Currency] = implicitly[Decoder[String]].emapTry { code =>
    Try {
      Currency.getInstance(code)
    }
  }

  implicit val currencyEncoder: Encoder[Currency] = implicitly[Encoder[String]].contramap(_.toString)

}

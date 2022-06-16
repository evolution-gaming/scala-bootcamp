package com.evolutiongaming.bootcamp.tf.practice.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.JsonCodec

import java.util.Currency
import scala.util.Try

@JsonCodec
final case class Money(amount: BigDecimal, currency: Currency)

object Money {

  implicit val currencyDecoder: Decoder[Currency] =
    implicitly[Decoder[String]].emapTry { code =>
      Try(Currency.getInstance(code))
    }

  implicit val currencyEncoder: Encoder[Currency] =
    implicitly[Encoder[String]].contramap(_.toString)
}

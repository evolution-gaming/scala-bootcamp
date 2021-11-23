package com.evolutiongaming.bootcamp.tf.practice.domain.payment

import io.circe.generic.JsonCodec

@JsonCodec
final case class PaymentId(id: String)

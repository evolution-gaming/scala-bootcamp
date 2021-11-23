package com.evolutiongaming.bootcamp.tf.practice.domain.cart

import io.circe.generic.JsonCodec

@JsonCodec
final case class Quantity(amount: Long)

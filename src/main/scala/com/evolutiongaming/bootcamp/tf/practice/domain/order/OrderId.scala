package com.evolutiongaming.bootcamp.tf.practice.domain.order

import io.circe.generic.JsonCodec

import java.util.UUID

@JsonCodec
final case class OrderId(value: UUID)

package com.evolutiongaming.bootcamp.tf.practice.domain.cart

import io.circe.generic.JsonCodec

import java.util.UUID

@JsonCodec
final case class ItemId(value: UUID)

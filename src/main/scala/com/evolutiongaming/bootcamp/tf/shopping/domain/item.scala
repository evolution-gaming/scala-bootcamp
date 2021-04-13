package com.evolutiongaming.bootcamp.tf.shopping.domain

import io.circe.generic.JsonCodec

import java.util.UUID

object item {

  @JsonCodec
  final case class ItemId(value: UUID)

}

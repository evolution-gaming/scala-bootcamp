package com.evolutiongaming.bootcamp.tf.shopping.domain

import io.circe.generic.JsonCodec

import java.util.UUID

object user {

  @JsonCodec
  final case class UserId(value: UUID)

}

package com.evolutiongaming.bootcamp.tf.practice.domain

import io.circe.generic.JsonCodec

import java.util.UUID

@JsonCodec
final case class UserId(value: UUID)

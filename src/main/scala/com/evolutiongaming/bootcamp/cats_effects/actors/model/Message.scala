package com.evolutiongaming.bootcamp.cats_effects.actors.model

import com.evolutiongaming.bootcamp.cats_effects.actors.Actor_3

case class Message(
  msg: String,
  replyTo: Actor_3[Int]
)

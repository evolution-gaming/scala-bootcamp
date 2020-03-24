package com.evolutiongaming.bootcamp.cats_effects.actors.model

import com.evolutiongaming.bootcamp.cats_effects.actors.Actor_2

case class Context[SenderIn](sender: Actor_2[SenderIn])

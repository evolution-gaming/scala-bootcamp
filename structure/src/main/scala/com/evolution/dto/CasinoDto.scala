package com.evolution.dto

import com.evolution.domain.Casino

final case class CasinoDto(id: String, name: String)

object CasinoDto {
  def from(casino: Casino): CasinoDto =
    apply(casino.id.value, casino.name.value)
}

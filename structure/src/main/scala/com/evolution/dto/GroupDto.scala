package com.evolution.dto

import com.evolution.domain.Group

final case class GroupDto(name: String)

object GroupDto {
  def from(group: Group): GroupDto = apply(group.name.value)
}

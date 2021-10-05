package com.evolution.dto

import com.evolution.domain.Permission

final case class PermissionDto(name: String)

object PermissionDto {
  def from(perm: Permission): PermissionDto = apply(perm.name.value)
}

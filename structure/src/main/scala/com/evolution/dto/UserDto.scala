package com.evolution.dto

import com.evolution.domain.User

final case class UserDto(login: String, name: String, lastName: String)

object UserDto {
  def from(user: User): UserDto =
    apply(user.login.value, user.firstName.value, user.lastName.value)
}

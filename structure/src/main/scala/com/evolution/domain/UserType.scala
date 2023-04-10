package com.evolution.domain

sealed trait UserType
object UserType {
  case object Operation extends UserType
  case object Player    extends UserType
}

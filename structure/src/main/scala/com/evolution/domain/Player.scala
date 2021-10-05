package com.evolution.domain

final case class Player(user: User) {
  val utype: UserType = UserType.Player
}

package com.evolution.domain

final case class Config(
  url: String,
  allowedCasinos: Boolean = true,
  showAllUsers: Boolean = true,
)

// final case class HttpConfig(url: String)
// final case class UserConfig(allowedCasinos: Boolean = true)
// final case class CasinoConfig(showAllUsers: Boolean = true)

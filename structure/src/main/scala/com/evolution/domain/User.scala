package com.evolution.domain

final case class UserId(value: String)        extends AnyVal
final case class UserLogin(value: String)     extends AnyVal
final case class UserFirstName(value: String) extends AnyVal
final case class UserLastName(value: String)  extends AnyVal

final case class User(
  id: UserId,
  login: UserLogin,
  firstName: UserFirstName,
  lastName: UserLastName,
)

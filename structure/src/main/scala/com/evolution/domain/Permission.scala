package com.evolution.domain

final case class PermissionName(value: String) extends AnyVal

final case class Permission(name: PermissionName)

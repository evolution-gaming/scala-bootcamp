package com.evolution.services

import com.evolution.domain.Permission
import com.evolution.repository.UserRepository

class PermissionService(userRepository: UserRepository) {
  def addPermission(value: Permission): Unit =
    userRepository.addPermission(value)
}

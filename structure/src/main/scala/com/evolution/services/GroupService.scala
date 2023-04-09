package com.evolution.services

import com.evolution.domain._
import com.evolution.repository.UserRepository

class GroupService(userRepository: UserRepository) {
  def addGroup(group: Group): Unit    = userRepository.addGroup(group)
  def deleteGroup(group: Group): Unit = userRepository.deleteGroup(group)
}

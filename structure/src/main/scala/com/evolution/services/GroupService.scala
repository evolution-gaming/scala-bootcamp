package com.evolution.services

import com.evolution.domain._
import com.evolution.repository.UserRepository

class GroupService(userRepository: UserRepository, config: Config) {
  def userGroups(user: User): List[Group] = userRepository.userGroups(user.id)
  def addGroup(group: Group): Unit = userRepository.addGroup(group)
}

package com.evolution.services

import com.evolution.domain._
import com.evolution.repository._

class UserService(
    userRepository: UserRepository,
    userAnalysisRepository: UserAnalysisRepository,
    userReportsRepository: UserReportsRepository,
    config: Config
) {
  def addUser(value: User): Unit = userRepository.addUser(value)
  def userById(id: UserId): Option[User] = userRepository.userById(id)
  def assignPermissions(
      group: GroupName,
      permissions: List[PermissionName]
  ): Unit = ()
}

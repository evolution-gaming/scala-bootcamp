package com.evolution.services

import com.evolution.domain._
import com.evolution.repository._

class UserService(
  userRepository: UserRepository,
  userAnalysisRepository: UserAnalysisRepository,
  userReportsRepository: UserReportsRepository,
  config: Config,
) {
  def addUser(value: User): Unit                    = userRepository.addUser(value)
  def userById(id: UserId): Option[User]            = userRepository.userById(id)
  def assignPermissions(
    group: GroupName,
    permissions: List[PermissionName],
  ): Unit = ()
  def userGroups(user: User): List[Group]           = userRepository.userGroups(user.id)
  def userPermissions(user: User): List[Permission] =
    userRepository.userPermissions(user.id)

  def report(user: User, reportId: ReportId): Option[Report] =
    userReportsRepository.report(user.id, reportId)

  def analysis(user: User): Option[Analysis] =
    userAnalysisRepository.analysis(user.id)
}

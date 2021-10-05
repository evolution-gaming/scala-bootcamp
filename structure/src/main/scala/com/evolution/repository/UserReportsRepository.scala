package com.evolution.repository

import com.evolution.domain.{Report, UserId}

class UserReportsRepository(repository: UserRepository)
    extends AbstractRepository {
  def report1(userId: UserId): Option[Report] = None
  def report2(userId: UserId): Option[Report] = None
  def report3(userId: UserId): Option[Report] = None
}

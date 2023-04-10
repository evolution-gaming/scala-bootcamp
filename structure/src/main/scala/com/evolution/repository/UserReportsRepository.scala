package com.evolution.repository

import com.evolution.domain.{Report, ReportId, UserId}

class UserReportsRepository(repository: UserRepository) extends AbstractRepository {
  def report(userId: UserId, reportId: ReportId): Option[Report] = None
}

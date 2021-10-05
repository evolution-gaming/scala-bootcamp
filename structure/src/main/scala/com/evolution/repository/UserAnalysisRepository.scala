package com.evolution.repository

import com.evolution.domain.{Analysis, UserId}

class UserAnalysisRepository(repository: UserRepository)
    extends AbstractRepository {
  def analyse(userId: UserId): Option[Analysis] = None
}

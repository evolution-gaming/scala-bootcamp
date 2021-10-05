package com.evolution.services

import com.evolution.domain.{Casino, CasinoId, Config}
import com.evolution.repository.CasinoRepository

import java.time.Instant

class CasinoService(repository: CasinoRepository, config: Config) {
  def lastUpdated: Instant = repository.lastUpdated

  def saveAll(casinos: List[Casino]): Unit = casinos.foreach(repository.save)

  def save(casino: Casino): Unit = repository.save(casino)

  def allCasinos: List[Casino] = repository.casinos

  def casinoById(id: CasinoId): Option[Casino] = repository.casinoById(id.value)
}

package com.evolution.repository

import com.evolution.domain.Casino

import java.time.Instant

class CasinoRepository extends AbstractRepository {
  def lastUpdated: Instant = Instant.now()
  def casinos: List[Casino] = Nil
  def casinoById(id: String): Option[Casino] = None
  def save(casino: Casino): Unit = ()
}

package com.evolution.controllers

import com.evolution.domain._
import com.evolution.dto.CasinoDto
import com.evolution.infrastructure.http.Controller
import com.evolution.services.CasinoService

class CasinoController(casinoService: CasinoService) extends Controller {
  // get: api/casinos
  def allCasinos: List[CasinoDto] = casinoService.allCasinos.map(CasinoDto.from)

  // post: api/casinos
  def update(casino: CasinoDto): Unit =
    casinoService.casinoById(CasinoId(casino.id)).foreach { c =>
      casinoService.save(
        Casino(c.id, CasinoName(casino.name))
      )
    }
}

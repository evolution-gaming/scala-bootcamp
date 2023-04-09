package com.evolution.domain

import java.time.Instant

final case class CasinoId(value: String)   extends AnyVal
final case class CasinoName(value: String) extends AnyVal

// TODO: use it
sealed abstract class CasinoStatus(val value: String, val code: Int)
object CasinoStatus {
  case object Active   extends CasinoStatus("Active", 1)
  case object Inactive extends CasinoStatus("Inactive", 0)
  val values: List[CasinoStatus]                 = List(Active, Inactive)
  def apply(value: String): Option[CasinoStatus] = values.find(_.value == value)
}

final case class Region(allowed: Boolean = true, status: Int = 1)

final case class Casino(
  id: CasinoId,
  name: CasinoName,
  regions: List[Region] = Nil,
  lastUpdated: Instant = Instant.now(),
) {
  override def toString: String = s"${name.value}-${id.value}"

  def allowedRegions: List[Region] =
    regions.filter(r => r.allowed && r.status == 1)

  def withAllowedRegions: Casino = copy(regions = allowedRegions)

//  def isActive: Boolean =
//    status == CasinoStatus.Active && allowedRegions.nonEmpty
//
//  def activate: Casino = copy(status = CasinoStatus.Active)
//  def deactivate: Casino = copy(status = CasinoStatus.Inactive)
}

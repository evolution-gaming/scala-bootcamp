package com.evolution.domain

import play.api.libs.json.Json.WithDefaultValues
import play.api.libs.json.{Format, Json, OFormat}

object CasinoJson {
  implicit val RegionFormat: Format[Region]             = Json.format
  implicit val CasinoIdFormat: Format[CasinoId]         = Json.valueFormat
  implicit val CasinoStatusFormat: Format[CasinoStatus] =
    implicitly[Format[String]]
      .bimap(CasinoStatus(_).getOrElse(CasinoStatus.Inactive), _.value)
  implicit val CasinoNameFormat: Format[CasinoName]     = Json.valueFormat
  implicit val CasinoFormat: OFormat[Casino]            =
    Json.using[WithDefaultValues].format
}

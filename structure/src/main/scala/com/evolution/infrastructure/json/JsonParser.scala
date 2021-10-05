package com.evolution.infrastructure.json

import com.evolution.domain.Casino
import com.evolution.domain.CasinoJson._
import com.evolution.infrastructure.utils.Utils
import play.api.libs.json.Json

class JsonParser {
  def parse(value: String): Option[Casino] =
    Json.parse(value).asOpt[Casino]

  def parseList(value: String): List[Casino] = {
    Utils.logDebug(value)
    Utils.validate(value).toList.flatMap { validated =>
      Json.parse(validated).as[List[Casino]]
    }
  }
}

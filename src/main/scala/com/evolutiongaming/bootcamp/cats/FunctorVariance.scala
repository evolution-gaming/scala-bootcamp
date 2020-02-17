package com.evolutiongaming.bootcamp.cats

import java.time.{Instant, LocalDate, ZoneOffset}

import cats.syntax.all._
import io.circe.{Codec, Decoder, Encoder}

object FunctorVariance {

  def instantToDate(instant: Instant): LocalDate = instant.atZone(ZoneOffset.UTC).toLocalDate

  def dateToInstant(date: LocalDate): Instant = date.atStartOfDay(ZoneOffset.UTC).toInstant

  val dateParser: Decoder[LocalDate] = ???

  val instantParser: Decoder[Instant] = ??? // implement me

  val dateWriter: Encoder[LocalDate] = ???

  val instantWriter: Encoder[Instant] = ??? // implement me

  val dateCodec: Codec[LocalDate] = ???

  val instantCodec: Codec[Instant] = ??? // implement me

}

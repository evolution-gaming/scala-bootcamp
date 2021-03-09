package com.evolutiongaming.bootcamp.json

import java.time.LocalDate

object Models {

  sealed trait Artist {
    def genre: Genre
    def gigs: Seq[Gig]
  }

  final case class Band(
    title: String,
    members: Seq[Musician],
    override val genre: Genre,
    override val gigs: Seq[Gig]
  ) extends Artist

  final case class SoloMusician(
    musician: Musician,
    override val genre: Genre,
    override val gigs: Seq[Gig]
  ) extends Artist

  final case class Musician(
    name: String,
    kind: MusicianKind
  )

  final case class Gig(
    venue: String,
    date: LocalDate,
    setlist: Seq[String]
  )

  sealed trait MusicianKind

  object MusicianKind {
    case object Singer extends MusicianKind

    case object Guitar extends MusicianKind

    case object Bass extends MusicianKind

    case object Drums extends MusicianKind

    case object Keys extends MusicianKind
  }

  sealed trait Genre

  object Genre {
    case object Rock extends Genre

    case object `Hip-Hop` extends Genre

    case object Pop extends Genre

    case object Jazz extends Genre
  }

}

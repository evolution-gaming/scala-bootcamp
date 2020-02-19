package com.evolutiongaming.bootcamp.json

import java.time.Year

import io.circe.Json
import io.circe.optics.JsonPath._
import io.circe.parser._
import io.circe.syntax._
import org.scalatest.Assertion
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class CirceExercisesSpec extends AnyWordSpec with Matchers {
  import CirceExercises._
  import CirceExercisesSpec._

  "JSON classes" should {
    "complete exercise 1" in {
      basics.jMatrix.noSpaces must equal(ex1Expected)
    }
    "complete exercise 2" in {
      basics.killersOnTourJson.noSpaces must equal(ex2Expected)
    }
    "complete exercise 3" in {
      optics.killersOnTourJson.noSpaces must equal(ex3Expected)
    }
    "complete exercise 4" in {
      val json = semiauto.albumJson
      checkAlbum(json)
    }
    "complete exercise 5" in {
      val json = manual.albumJson
      checkAlbum(json)
    }
    "complete exercise 6" in {
      val year1999 = Year.of(1999)
      import custom1.{encodeYear, decodeYear}
      decode[Int](year1999.asJson.noSpaces) must be(Right(year1999.getValue))
      Json.fromInt(2020).as[Year] must be(Right(Year.of(2020)))
    }
    "complete exercise 7" in {
      ex7ArtistTitles.getAll(adt.artistsJson).toSet must be(Set(("The Killers")))
    }
  }

  private def checkAlbum(json: Json): Assertion = {
    ex4AlbumTitle.getOption(json).isDefined must be(true)
    ex4AlbumYear.getOption(json).isDefined must be(true)
    val songs = ex4AlbumSongs1.getOption(json)
    songs.isDefined must be(true)
    ex4AlbumSongs2.getAll(json).size must be(songs.map(_.size).getOrElse(-1))
  }
}

object CirceExercisesSpec {
  private val ex1Expected = """{"title":"The Matrix","year":1999,"actors":["Keanu Reeves","Carrie-Anne Moss","Laurence Fishburne"],"isRatedR":true}"""
  private val ex2Expected = """{"artist":{"name":"The Killers","ontour":true,"stats":{"listeners":4517050,"playcount":216877854},"genres":["indie rock","alternative rock","new wave"],"members":[{"name":"Brandon Flowers","instruments":["vocals","keyboard","bass"]},{"name":"Dave Keuning","instruments":["lead guitar"]},{"name":"Mark Stoermer","instruments":["bass","rhythm guitar"]},{"name":"Ronnie Vanucci Jr.","instruments":["drums","percussion"]}],"url":"https://www.last.fm/music/The+Killers"}}"""
  private val ex3Expected = ex2Expected
  private val ex4AlbumTitle = root.title.string
  private val ex4AlbumYear = root.year.int
  private val ex4AlbumSongs1 = root.songs.arr
  private val ex4AlbumSongs2 = root.songs.each.title.string

  private val ex7ArtistTitles = root.each.title.string
}

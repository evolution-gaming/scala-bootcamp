package com.evolutiongaming.bootcamp.json

import org.scalatest.wordspec.AnyWordSpec
import scalaj.http.{Http, HttpResponse}

class HomeworkSpec extends AnyWordSpec {
  "JSON classes" should {
    "parse response" in {
      val resp = HomeworkSpec.fetchResponse()
    }
  }
}

object HomeworkSpec {
  sealed trait Gender
  object Gender {
    case object M
    case object F
    case object N
  }
  final case class User(age: String, country: String, gender: Gender)
  final case class UserInfo(user: User)

  def fetchResponse(online: Boolean = true): String = {
    if (online) {
      val response: HttpResponse[String] = Http("http://foo.com/search").param("q","monkeys").asString
      response.body
    } else {
      response
    }
  }

  private val response: String = """
                                   |{
                                   |    "user": {
                                   |        "age": "0",
                                   |        "bootstrap": "0",
                                   |        "country": "United States",
                                   |        "gender": "n",
                                   |        "image": [
                                   |            {
                                   |                "#text": "https://lastfm.freetls.fastly.net/i/u/34s/22beec8570014d6fcdfb7871a2cc29cf.png",
                                   |                "size": "small"
                                   |            },
                                   |            {
                                   |                "#text": "https://lastfm.freetls.fastly.net/i/u/64s/22beec8570014d6fcdfb7871a2cc29cf.png",
                                   |                "size": "medium"
                                   |            },
                                   |            {
                                   |                "#text": "https://lastfm.freetls.fastly.net/i/u/174s/22beec8570014d6fcdfb7871a2cc29cf.png",
                                   |                "size": "large"
                                   |            },
                                   |            {
                                   |                "#text": "https://lastfm.freetls.fastly.net/i/u/300x300/22beec8570014d6fcdfb7871a2cc29cf.png",
                                   |                "size": "extralarge"
                                   |            }
                                   |        ],
                                   |        "name": "travisbrown",
                                   |        "playcount": "76111",
                                   |        "playlists": "0",
                                   |        "realname": "Travis Brown",
                                   |        "registered": {
                                   |            "#text": 1126389912,
                                   |            "unixtime": "1126389912"
                                   |        },
                                   |        "subscriber": "0",
                                   |        "type": "user",
                                   |        "url": "https://www.last.fm/user/travisbrown"
                                   |    }
                                   |}
                                   |""".stripMargin
}

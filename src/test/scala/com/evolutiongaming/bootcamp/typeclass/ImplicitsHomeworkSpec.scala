package com.evolutiongaming.bootcamp.typeclass

import com.evolutiongaming.bootcamp.typeclass.ImplicitsHomework.SuperVipCollections4s._
import com.evolutiongaming.bootcamp.typeclass.ImplicitsHomework.SuperVipCollections4s.syntax._
import com.evolutiongaming.bootcamp.typeclass.ImplicitsHomeworkSpec.TestValue
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class ImplicitsHomeworkSpec extends AnyFreeSpec with Matchers {

  "MutableBoundedCache" - {
    val val1 = TestValue(1)
    val val2 = TestValue(2)
    val val3 = TestValue(3)
    val val4 = TestValue(4)
    val val5 = TestValue(5)

    "when enough capacity should store values" in {
      val cache = new MutableBoundedCache[TestValue, TestValue](maxSizeScore = Int.MaxValue)

      cache.put(val1, val2)
      cache.put(val3, val4)
      cache.get(val1) shouldEqual Some(val2)
      cache.get(val3) shouldEqual Some(val4)

      cache.put(val1, val5) //overwrite
      cache.get(val1) shouldEqual Some(val5)
    }
    "when not enough capacity should evict first inserted values" in {
      val cache = new MutableBoundedCache[TestValue, TestValue](maxSizeScore = 12)

      cache.put(val3, val3) //score +6
      cache.put(val2, val2) //score +4
      cache.put(val1, val1) //score +2, total score 12
      cache.get(val3) shouldEqual Some(val3)
      cache.get(val2) shouldEqual Some(val2)
      cache.get(val1) shouldEqual Some(val1)

      cache.put(val4, val4) //score +8, first 2 should be evicted
      cache.get(val3) shouldEqual None
      cache.get(val2) shouldEqual None
      cache.get(val1) shouldEqual Some(val1)
      cache.get(val4) shouldEqual Some(val4)
    }
  }

  "Iterate2 instances provided" - {
    "for Map" in {
      assertCompiles(
        """import com.evolutiongaming.bootcamp.typeclass.ImplicitsHomework.SuperVipCollections4s._
          |import com.evolutiongaming.bootcamp.typeclass.ImplicitsHomework.SuperVipCollections4s.instances._
          |
          |implicitly[Iterate2[Map]]
          |""".stripMargin
      )
    }
    "for PackedMultiMap" in {
      assertCompiles(
        """import com.evolutiongaming.bootcamp.typeclass.ImplicitsHomework.SuperVipCollections4s._
          |import com.evolutiongaming.bootcamp.typeclass.ImplicitsHomework.SuperVipCollections4s.instances._
          |
          |implicitly[Iterate2[PackedMultiMap]]
          |""".stripMargin
      )
    }
  }

  "Size scores of primitives" - {
    import com.evolutiongaming.bootcamp.typeclass.ImplicitsHomework.SuperVipCollections4s.instances._

    "Byte" in {
      127.toByte.sizeScore shouldEqual 1
    }
    "Char" in {
      'E'.sizeScore shouldEqual 2
    }
    "Int" in {
      13.sizeScore shouldEqual 4
    }
    "Long" in {
      1024L.sizeScore shouldEqual 8
    }
  }

  "Size score of collections" - {
    import com.evolutiongaming.bootcamp.typeclass.ImplicitsHomework.SuperVipCollections4s.instances._

    "String" in {
      "".sizeScore shouldEqual 12
      "8".sizeScore shouldEqual (12 + 2)
      "qweasdzxc".sizeScore shouldEqual (12 + 2 * 9)
    }
    "List" in {
      List.empty[TestValue].sizeScore shouldEqual 12
      List(
        TestValue(1),
        TestValue(2),
        TestValue(3),
      ).sizeScore shouldEqual (12 + 1 + 2 + 3)
    }
    "Vector" in {
      Vector.empty[TestValue].sizeScore shouldEqual 12
      Vector(
        TestValue(4),
        TestValue(5),
        TestValue(6),
      ).sizeScore shouldEqual (12 + 4 + 5 + 6)
    }
    "Array" in {
      Array.empty[TestValue].sizeScore shouldEqual 12
      Array(
        TestValue(30),
        TestValue(20),
        TestValue(10),
      ).sizeScore shouldEqual (12 + 30 + 20 + 10)
    }
    "Map" in {
      Map.empty[TestValue, TestValue].sizeScore shouldEqual 12
      Map(
        TestValue(1) -> TestValue(2),
        TestValue(3) -> TestValue(4),
        TestValue(5) -> TestValue(6),
      ).sizeScore shouldEqual (12 + 1.to(6).sum)
    }
    "PackedMultiMap" in {
      PackedMultiMap.empty[TestValue, TestValue].sizeScore shouldEqual 12
      PackedMultiMap(
        TestValue(1) -> TestValue(2),
        TestValue(3) -> TestValue(4),
        TestValue(5) -> TestValue(6),
      ).sizeScore shouldEqual (12 + 1.to(6).sum)
    }
  }

  "TwitCache" - {
    import com.evolutiongaming.bootcamp.typeclass.ImplicitsHomework.MyTwitter._

    val id1 = 1L //8
    val twit1 = Twit( //12
      id = id1, //8
      userId = 1, //4
      hashTags = Vector.empty, //12
      attributes = PackedMultiMap.empty, //12
      fbiNotes = Nil, //12
    ) //first entry score: 8 *2 + 4*12 + 4 = 68

    val id2 = 2L
    val twit2 = Twit(
      id2,
      userId = 2,
      hashTags = Vector("foodie"), //+12 + 6 * 2 for chars = +24
      attributes = PackedMultiMap("hasNoFriends" -> "true"), //2 * 12 + 16 * 2 for chars = +56
      fbiNotes = List(FbiNote( //+12
        month = "september", //+ 12 + 9 * 2 = +30
        favouriteChar = 'E', //+2
        watchedPewDiePieTimes = 2568L, //+8
      ))
    ) //second entry score: 68 + 24 + 56 + 12 + 30 + 2 + 8 = 200

    "should limit the size score of data stored" in {
      val cache = createTwitCache(maxSizeScore = 200)

      cache.put(twit1)
      cache.get(id1) shouldEqual Some(twit1)

      cache.put(twit2)
      cache.get(id1) shouldEqual None
      cache.get(id2) shouldEqual Some(twit2)
    }
  }
}

private object ImplicitsHomeworkSpec {
  case class TestValue(score: SizeScore)
  object TestValue {
    implicit val testValueGetSizeScore: GetSizeScore[TestValue] = (value: TestValue) => value.score
  }
}

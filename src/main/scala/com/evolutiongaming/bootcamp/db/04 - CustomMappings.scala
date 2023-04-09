package com.evolutiongaming.bootcamp.db

import cats.effect._
import cats.implicits._
import doobie._
import doobie.implicits._

object CustomMappings extends IOApp {

  final case class Coordinate(x: Int, y: Int)
  final case class Point(name: String, coordinate: Coordinate)

//  object Coordinate {
//    def fromVarchar(raw: String): Coordinate = {
//      val xy = raw.split(",").map(_.toInt)
//      Coordinate(xy.head, xy.last)
//    }
//    def toVarchar(c: Coordinate): String =
//      s"${c.x},${c.y}"
//  }

  // define `Get`, `Put`, `Meta`, talk about `Read` and `Write`, untagged `.map` and `.contramap` vs tagged ones
//  implicit val xyGet: Get[Coordinate] = Get[String].tmap(Coordinate.fromVarchar)
//  implicit val xyPut: Put[Coordinate] = Put[String].tcontramap(Coordinate.toVarchar)
  // OR untagged `.map` vs tagged `.timap`
//  implicit val xyMeta: Meta[Coordinate] = Meta[String].timap(Coordinate.fromVarchar)(Coordinate.toVarchar)

  override def run(args: List[String]): IO[ExitCode] = {
    val xa = DbTransactor.make[IO]
    setup().transact(xa) *>
      selectAll().nel
        .transact(xa)
        .map(println)
        .as(ExitCode.Success)
  }

  def selectAll(): Query0[Point] =
    sql"select name, x, y from points".query[Point]

  def createTable(): doobie.Update0 =
    sql"create table points(name VARCHAR PRIMARY KEY, x INT, y INT)".update

  def setup(): ConnectionIO[Unit] = {
    val create = createTable().run
//    val create = sql"create table points(name VARCHAR PRIMARY KEY, xy VARCHAR)".update.run
    val insert = "insert into points(name, x, y) values (?, ?, ?)"
//    val insert = "insert into points(name, xy) values (?, ?)"

    // batch insert
    def insertPoints(rawValues: List[(String, Int, Int)]): ConnectionIO[Int] =
      Update[(String, Int, Int)](insert).updateMany(rawValues)
//      Update[(String, String)](insert).updateMany(rawValues.map { case (n, x, y) => (n, s"$x,$y") })

    val inserts = insertPoints(
      List(
        ("A", 0, 0),
        ("B", 5, 5),
        ("C", 10, 10),
      )
    )

    (create, inserts).mapN(_ + _).as(())
  }
}

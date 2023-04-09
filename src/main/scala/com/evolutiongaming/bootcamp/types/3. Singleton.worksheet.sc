//format: off
/**
 * ╔═══════════════════════════╗
 * ║    S i n g l e t o n s    ║
 * ╚═══════════════════════════╝
 */










 


//format: on

type S_123 = "123"

def askSingletons(x: "123", y: 5, z: 4.3) = ()

askSingletons("123", 5, 4.3)

// askSingletons("12", 5, 4.3)

//format: off









// ┌────────────────────────┐
// │ Type-level programming │
// └────────────────────────┘

//format: on

case class Person(name: String, age: Int)

sealed abstract class PersonTL[+name <: String, +age <: Int]

object PersonTL {
  case object person extends PersonTL[Nothing, Nothing]

  // add with Singleton
  def apply[name <: String with Singleton, age <: Int with Singleton](
    name: name,
    age: age,
  ): PersonTL[name, age] = person
}

PersonTL("Oleg", 37)

val persons = (
  PersonTL("Oleg", 37),
  PersonTL("Tbilisi", 1568),
  PersonTL("Miyuki", 16),
)

object Collect {
  import shapeless._

  import shapeless.syntax.std.tuple._

  object Materialize extends Poly1 {
    implicit def casePersonTL[name <: String, age <: Int](implicit
      name: ValueOf[name],
      age: ValueOf[age],
    ) = at[PersonTL[name, age]](person => Person(name.value, age.value))
  }

  val materialized = persons.map(Materialize).toList

}

Collect.materialized

// format: off




















//format: on

val xList = List(1, 2, 3)

val yList: xList.type = xList

// can't be more specific than singleton

val x2: 2 = 2

val x3: x2.type = 2

//
// val xInt = 2

// val yInt: xInt.type = 2

// format: off








/**
 * ┌──────────────────┐
 * │ Fluent interface │
 * └──────────────────┘
 */
//format: on

// new PersonBuilder
// .name("Oleg")
// .age(32)

import scala.collection.mutable.Buffer
import scala.collection.mutable.ReusableBuilder

class VectorBuilder extends ReusableBuilder[Int, Vector[Int]] {

  val elements = Buffer.empty[Int]

  override def clear(): Unit = elements.clear()

  override def result() = elements.toVector

  override def addOne(elem: Int): this.type = {
    elements += elem
    // new VectorBuilder
    this
  }
}
// format: off














// ┌─────────────────────────┐
// │ Type-member unification │
// └─────────────────────────┘
// format: on

trait Entity {
  type EntityData
  type EntityId
}

// abstract class EntityReader(val e: Entity) {
//   def read(id: e.EntityId): Option[e.EntityData]
// }

// abstract class EntityCodecs(val e: Entity) {
//   def encodeData(data: e.EntityData): String
//   def decodeData(data: String): Either[String, e.EntityData]

//   def encodeId(id: e.EntityId): String
//   def decodeId(id: String): Either[String, e.EntityId]
// }

// class EntityService(val e: Entity)(reader: EntityReader, codecs: EntityCodecs) {
//   def read(id: String): Either[String, String] = for {
//     id <- codecs.decodeId(id)
//     data <- reader.read(id).toRight("Not found")
//   } yield codecs.encodeData(data)

// }

abstract class EntityReader[e <: Entity with Singleton](val e: e) {
  def read(id: e.EntityId): Option[e.EntityData]
}

abstract class EntityCodecs[e <: Entity with Singleton](val e: e) {
  def encodeData(data: e.EntityData): String
  def decodeData(data: String): Either[String, e.EntityData]

  def encodeId(id: e.EntityId): String
  def decodeId(id: String): Either[String, e.EntityId]
}

abstract class EntityService[e <: Entity with Singleton](val e: e) {
  def reader: EntityReader[e.type]
  def codecs: EntityCodecs[e.type]

  def read(id: String): Either[String, String] = for {
    id   <- codecs.decodeId(id)
    data <- reader.read(id).toRight("Not found")
  } yield codecs.encodeData(data)
}

object EntityService {
  def apply(e: Entity)(
    eReader: EntityReader[e.type],
    eCodecs: EntityCodecs[e.type],
  ): EntityService[e.type] = new EntityService[e.type](e) {
    override val reader: EntityReader[e.type] = eReader
    override val codecs: EntityCodecs[e.type] = eCodecs
  }
}

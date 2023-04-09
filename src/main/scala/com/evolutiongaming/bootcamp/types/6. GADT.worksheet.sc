// format: off
/**
 * ╔═══════════════════════════════════════════════╗
 * ║ G e n e r a l i z e d    A l g e b r a i c    ║
 * ║            D a t a    T y p e s               ║
 * ╚═══════════════════════════════════════════════╝
 */


// format: on

// NORMAL ADT

sealed trait Parent[A, B]
object Parent {
  case class Child1[A, B](as: List[A], bs: List[B]) extends Parent[A, B]
  case class Child2[A, B](a: A, b: B)               extends Parent[A, B]
}

// Parent[String, Int]
// Child1[String, Int]
// Child2[String, Int]

// Generalized ADT

sealed trait Parent1[A, B]
object Parent1 {
  case class Child1[A, B, C, D](a: A, b: B, c: C, d: D) extends Parent1[A, B]
  case class Child2[C, D](c: C, d: D)                   extends Parent1[String, (C, D)]
}

// Parent1[Double, String]
// format: off








// format: on

// ┌───────────────────┐
// │ Existential Types │
// └───────────────────┘
sealed trait InfoGetter
object InfoGetter {
  case class Impl[A](
    getInfo: Long => A,
    printInfo: A => String,
  ) extends InfoGetter
}

def getInfos(keys: Long*)(getter: InfoGetter): Vector[String] =
  getter match {
    case InfoGetter.Impl(getInfo, printInfo) =>
      keys.view.map(k => printInfo(getInfo(k))).toVector
  }

// def getInfos2(keys: Long*)(getter: InfoGetter): Vector[String] =
//   getter match {
//     case getter: InfoGetter.Impl[a] =>
//       val getInfo: Long => a = getter.getInfo
//       val printInfo: a => String = getter.printInfo
//       keys.view.map(k => printInfo(getInfo(k))).toVector
//   }

// format: off













// format: on

// ┌───────────────┐
// │ Type Indexes  │
// └───────────────┘
// List[String]

sealed trait Tag[A]

object Tag {
  case object OfInt                            extends Tag[Int]
  case object OfString                         extends Tag[String]
  case class Tuple[A, B](a: Tag[A], b: Tag[B]) extends Tag[(A, B)]
}

case class TaggedVector[A](tag: Tag[A], vec: Vector[A])

def combineAll[A](lst: TaggedVector[A]): A = lst match {
  case TaggedVector(Tag.OfInt, vec)             => vec.sum
  case TaggedVector(Tag.OfString, vec)          => vec.mkString(",")
  case TaggedVector(Tag.Tuple(tagA, tagB), vec) =>
    (
      combineAll(TaggedVector(tagA, vec.map(_._1))),
      combineAll(TaggedVector(tagB, vec.map(_._2))),
    )
}

combineAll(
  TaggedVector(
    Tag.Tuple(Tag.OfInt, Tag.Tuple(Tag.OfInt, Tag.OfString)),
    Vector((1, (2, "3")), (4, (5, "6"))),
  )
)

// format: off





















// format: on

// ┌────────────────────────┐
// │ Polymorphic Recursion  │
// └────────────────────────┘

val bigCompose =
  1.to(100000).foldLeft((x: Int) => x)((f, _) => f.andThen(_ + 1))

// List(1, 2, 3).map(bigCompose)
import scala.annotation.tailrec

case class Compose[A, B, C](first: A => B, second: B => C) extends (A => C) {
  def apply(a: A): C                         = Compose.run(this, a)
  override def andThen[D](g: C => D): A => D = Compose(this, g)
  override def compose[D](g: D => A): D => C = Compose(g, this)
}

object Compose {
  @tailrec def run[A, B](f: A => B, x: A): B = f match {
    case Compose(a, b) =>
      a match {
        case Compose(u, v) => run(Compose(u, Compose(v, b)), x)
        case _             => run(b, a(x))
      }
    case f             => f(x)
  }
}

val bigCompose1 =
  1.to(100000).foldLeft[Int => Int](x => x)((f, _) => f.andThen(_ + 1))

// List(1, 2, 3).map(bigCompose1)
//
//

//
//

@tailrec final def run1[A, B](f: A => B, x: A): B = f match {
  case fc: Compose[A, x, B] =>
    type X = x
    fc.first match {
      case ac: Compose[A, y, x] =>
        run1[A, B](
          Compose[A, y, B](ac.first, Compose[y, x, B](ac.second, fc.second)),
          x,
        )
      case _                    => run1[X, B](fc.second, fc.first(x))
    }
  case f                    => f(x)
}

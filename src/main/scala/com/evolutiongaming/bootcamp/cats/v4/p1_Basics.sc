// Basic usage
{
  // Cats provides most of functionality via typeclasses
  // You'll need the typeclass itself
  import cats.Show
  // Syntax extensions for that typeclass
  import cats.syntax.show._
  // Typeclass instances: currently those are available without imports
  // On older cats versions those required an additional import
  import cats.instances.string._

  Show[String].show("foo")
  "foo".show

  // Imports all syntax extensions (and makes auto-completion overwhelming)
  // import cats.syntax.all._
  // Imports all implicits, used to mean "syntax + instances"
  // import cats.implicits._

  // https://impurepics.com/posts/2018-04-02-cats-imports.html
  // https://impurepics.com/posts/2018-07-07-cats-imports-difficulty.html
}

// Convenience operations for Option

import cats.syntax.option._

1.some
none[Int]
Some(1)
None

import cats.syntax.semigroup._

Option(1) |+| Some(2)
// Some(1) |+| Option(2)
// Some(1) |+| None
1.some |+| Option(2)
1.some |+| none

// And for Either

import cats.syntax.either._

1.asRight
val leftString = "error".asLeft[Int]
leftString.leftMap(_.toUpperCase)

2.asRight[Throwable].valueOr(throw _)

// Some datatypes
import cats.data.NonEmptyList

NonEmptyList.of(1, 2, 3)
NonEmptyList(1, List(2, 3))
NonEmptyList.fromList(List(1))
NonEmptyList.fromList(Nil)

import cats.data.NonEmptySet

NonEmptySet.of(1, 2, 2, 3)

// Show

import cats.Show

Show[String].show("foobar")

case class Foo(anInt: Int)

object Foo {
  // Usually you can delegate to .toString like this
  //    implicit val fooShow: Show[Foo] = Show.fromToString

  // Let's instead define something custom for clarity
  implicit val fooShow: Show[Foo] = foo => s"Foo:${foo.anInt}"
}

val foo = Foo(3)

Show[Foo].show(foo)

case class NoShow(int: Int)

val noShow = NoShow(42)

//Show[NoShow].show(noShow) // doesn't compile

import cats.syntax.show._

1.show
foo.show
//  noShow.show // also doesn't compile

// There's a string interpolator included
show"int: ${1}, str: ${"foo"}, foo: $foo"
//  show"does not compile: $noShow"

// Eq

import cats.Eq

// It can be used directly
Eq[Int].eqv(1, 1)
Eq[Int].neqv(1, 2)

// This one is way better with syntax

import cats.syntax.eq._

val ok               = 1 == 1
val technicallyFalse = 1 == "22" // compiles, but probably a bug

1 === 1
1 =!= 2
//1 === "1" // does not compile

// Let's define Eq for Foo
implicit val fooEq: Eq[Foo] = Eq.fromUniversalEquals

Foo(1) =!= Foo(2)

// Order

import cats.Order
import cats.syntax.order._

implicit val fooOrder: Order[Foo] = Order.by(_.anInt)

Foo(1) <= Foo(2)

package com.evolutiongaming.bootcamp.variance

import cats.Applicative
import cats.data.OptionT

// https://docs.scala-lang.org/tour/variances.html
object Variance {

  sealed trait Fruit

  sealed trait Apple     extends Fruit
  case object RedApple   extends Apple
  case object GreenApple extends Apple

  case object Orange extends Fruit



  // By default classes are invariant in Scala, i.e. subtyping relation of container's type parameters
  // does not affect subtyping of the container
  final case class InvariantBox[A](values: List[A]) {
    def add(value: A): InvariantBox[A] = InvariantBox(value :: values)
  }

  var apples1: InvariantBox[Apple] = InvariantBox(List(RedApple, GreenApple))
  apples1 = apples1.add(RedApple)
//  val fruits1: InvariantBox[Fruit] = apples1



  // Covariance is a direct mapping of subtyping relation, i.e. if A extends B then F[A] extends F[B] as well
  final case class CovariantBox[+A](values: List[A]) {
//    def add(value: A): CovariantBox[A] = CovariantBox(value :: values) // does not compile
  }

  val apples2: CovariantBox[Apple] = CovariantBox(List(RedApple, GreenApple))
  val fruits2: CovariantBox[Fruit] = apples2

  /*
    Usually, all types "holding" or "returning" values are covariant in their type parameter, e.g:
      List[+A]
      Option[+A]
      Either[+A, +B]

    Contravariance is an opposite mapping of subtyping relation, i.e. F[A] extends F[B] if B extends A

    Types which "require" or "consume" values are usually contravariant in their type parameter

    A good example of such type would be a Function, which is contravariant in its argument and covariant in return value:
    Function[-A, +R]
    or
    def function(argument: -A): +R
   */

  type FunctionOfSomeArg[-A] = Function[A, String]

  val showFruit: FunctionOfSomeArg[Fruit] = _.toString
  // Apple extends Fruit -> Function[Fruit, *] extends Function[Apple, *]
  val showApple: FunctionOfSomeArg[Apple] = showFruit



  // The last is polyvariance which is a special case of invariance for higher kinds that
  // takes the relation from its parameter dynamically
  final case class BoxOfBoxes[A](value: A)

//  val boxOfFruits1:  BoxOfBoxes[InvariantBox[Fruit]] = BoxOfBoxes(InvariantBox[Apple](List(RedApple, GreenApple)))
  val boxOfFruits2:  BoxOfBoxes[CovariantBox[Fruit]] = BoxOfBoxes(CovariantBox[Apple](List(RedApple, GreenApple)))
  val boxOfFuntions: BoxOfBoxes[Apple => String]     = BoxOfBoxes(showFruit)



  // Also you can set variance restrictions on nested type parameters
  final case class BoxOfBoxes2[F[_]](value: F[_])

//  BoxOfBoxes2(InvariantBox(List(RedApple)))
//  BoxOfBoxes2(CovariantBox(List(RedApple)))
  BoxOfBoxes2[-* => String](showFruit)


  def test[F[_]: Applicative](n: Int): OptionT[F, Int] = {
    import cats.syntax.applicative._
    if (n < 0) OptionT(Option.empty.pure[F])
    else OptionT(Option(n).pure[F])
  }
}

package com.evolutiongaming.bootcamp.typeclass

object FoldableExercise {
  trait Foldable[F[_]] {
    def foldLeft[T, S](ft: F[T], s: S)(f: (S, T) => S): S
  }

  implicit class FoldableOps[F[_]: Foldable, T](ft: F[T]) {
    def foldLeft[S](s: S)(f: (S, T) => S): S = implicitly[Foldable[F]].foldLeft(ft, s)(f)
  }

  implicit val optionFoldable: Foldable[Option] = new Foldable[Option] {
    override def foldLeft[T, S](ft: Option[T], s: S)(f: (S, T) => S): S =
      ft match {
        case None    => s
        case Some(t) => f(s, t)
      }
  }
  implicit val listFoldable: Foldable[List] = new Foldable[List] {
    override def foldLeft[T, S](ft: List[T], s: S)(f: (S, T) => S): S =
      ft.foldLeft(s)(f)
  }

  final case class Triple[T](
    v1: T,
    v2: T,
    v3: T,
  )

  /*
  Part 1.

  Define an Foldable instance for Triple (should behave like a collection of 3 elements)
   */

  //implicit val tripleFoldable: Foldable[Triple] = ???

  /*
  Part 2.

  Define another type-class - Summable[T] which should give us methods:
  - def plus(left: T, right: T): T
  - def zero: T

  Define Summable[T] instances for:
  - any T which has the standard library Numeric[T] type-class provided
  - Set[S] - zero should be Set.empty and plus should merge sets with + operation
   */

  /*
  Part 3.

  And finally - define generic collection sum method which works on any F[T]
  where F is Foldable (F[_]: Foldable) and T is Summable (T: Summable)!

  def genericSum... - work out the right method signature, should take F[T] and return T
   */

}

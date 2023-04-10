package com.evolutiongaming.bootcamp.typeclass.v3_typeclass

/** Try to accomplish as many tasks as you can
  */
object TypeClassesHomework {

  object OrderingTask {

    final case class Money(amount: BigDecimal)

    implicit val moneyOrdering: Ordering[Money] = ??? // TODO Implement Ordering instance for Money
  }

  object ShowTask {

    trait Show[T] { // Fancy toString
      def show(entity: T): String
    }

    final case class User(id: String, name: String)

    // TODO Implement Show instance for User

    // TODO Implement syntax for Show so I can do User("1", "John").show
  }

  object ParseTask {

    type Error = String

    trait Parse[T] { // Feel free to use any format. It could be CSV or anything else.
      def parse(entity: String): Either[Error, T]
    }

    final case class User(id: String, name: String)

    // TODO Implement Parse instance for User

    // TODO Implement syntax for Parse so I can do "lalala".parse[User] (and get an error because it is obviously not a User)
  }

  object EqualsTask {
    // TODO Design a typesafe equals so I can do a === b, but it won't compile if a and b are of different types
    // Define the typeclass (think of a method signature)
    // Keep in mind that `a method b` is `a.method(b)`
  }

  object Foldable {

    trait Semigroup[A] {
      def combine(x: A, y: A): A
    }

    trait Monoid[A] extends Semigroup[A] {
      def empty: A
    }

    trait Foldable[F[_]] {
      def foldLeft[A, B](as: F[A])(z: B)(f: (B, A) => B): B
      def foldRight[A, B](as: F[A])(z: B)(f: (A, B) => B): B
      def foldMap[A, B](as: F[A])(f: A => B)(implicit monoid: Monoid[B]): B
    }

    implicit val optionFoldable: Foldable[Option] = ??? // TODO Implement Foldable instance for Option

    implicit val listFoldable: Foldable[List] = ??? // TODO Implement Foldable instance for List

    sealed trait Tree[A]
    object Tree {
      final case class Leaf[A](value: A)                        extends Tree[A]
      final case class Branch[A](left: Tree[A], right: Tree[A]) extends Tree[A]
    }

    implicit val treeFoldable: Foldable[Tree] = ??? // TODO Implement Foldable instance for Tree
  }

  object ApplicativeTask {

    trait Semigroupal[F[_]] {
      def product[A, B](fa: F[A], fb: F[B]): F[(A, B)]
    }

    trait Functor[F[_]] {
      def map[A, B](fa: F[A])(f: A => B): F[B]
    }

    trait Apply[F[_]] extends Functor[F] with Semigroupal[F] {

      def ap[A, B](fab: F[A => B])(fa: F[A]): F[B] // "ap" here stands for "apply" but it's better to avoid using it

      override def product[A, B](fa: F[A], fb: F[B]): F[(A, B)] = ??? // TODO Implement using `ap` and `map`

      def map2[A, B, Z](fa: F[A], fb: F[B])(f: (A, B) => Z): F[Z] = ??? // TODO Implement using `map` and `product`
    }

    trait Applicative[F[_]] extends Apply[F] {
      def pure[A](a: A): F[A]
    }

    // TODO Implement Applicative instantce for Option
    implicit val optionApplicative: Applicative[Option] = ??? // Keep in mind that Option has flatMap

    // TODO Implement traverse using `map2`
    def traverse[F[_]: Applicative, A, B](as: List[A])(f: A => F[B]): F[List[B]] = ???

    // TODO Implement sequence (ideally using already defined things)
    def sequence[F[_]: Applicative, A](fas: List[F[A]]): F[List[A]] =
      traverse(fas)(fa => fa)
  }
}

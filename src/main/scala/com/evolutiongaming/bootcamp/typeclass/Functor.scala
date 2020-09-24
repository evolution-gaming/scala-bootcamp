package com.evolutiongaming.bootcamp.typeclass

// Functor trait
// Syntax
// Our list functor
// Map functor
// Type lambda
// Kind projector

trait Functor[F[_]] {

  def map[A, B](fa: F[A])(f: A => B): F[B]

}

object Functor {
  import MyList._

  implicit class FunctorOps[F[_]: Functor, A](fa: F[A]) {
    def map[B](f: A => B): F[B] = {
      implicitly[Functor[F]].map(fa)(f)
    }
  }

  implicit val listFunctor = new Functor[List] {
    override def map[A, B](fa: List[A])(f: A => B): List[B] = fa match {
      case Cons(head, tail) => Cons(f(head), map(tail)(f))
      case Nil => Nil
    }
  }

  val list: List[Int] = Cons(1, Nil)
  list.map(_.toString)

//  Cons(1, Nil).map(_.toString)

  implicit def mapFunctor[K]: Functor[({type L[A] = Map[K, A]})#L] = {
    type OneHoleMap[T] = Map[K, T]
    new Functor[OneHoleMap] {
      override def map[A, B](fa: OneHoleMap[A])(f: A => B): OneHoleMap[B] = {
        fa.view.mapValues(f).toMap
      }
    }
  }

  implicit def mapFunctorKindProjector[K]: Functor[Map[K, *]] = {
    new Functor[Map[K, *]] {
      override def map[A, B](fa: Map[K, A])(f: A => B): Map[K, B] = {
        fa.view.mapValues(f).toMap
      }
    }
  }
}

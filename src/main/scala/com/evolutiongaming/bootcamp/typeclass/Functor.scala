package com.evolutiongaming.bootcamp.typeclass

trait Functor[F[_]] {

  def map[A, B](fa: F[A])(f: A => B): F[B]

}

object Functor {
  import com.evolutiongaming.bootcamp.adt.MyList._

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

}

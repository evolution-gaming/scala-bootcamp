package com.evolutiongaming.bootcamp.variance

object Cothings {

  /*
                                         Invariant
                                             |
                            |------------------------------------|
                      Contravariant                       Functor (covariant)
                            |                                    |
              Divisible/ContravariantMonoidal               Applicative
                            |                                    |
                         Comonad                               Monad
   */


  trait Invariant[F[_]] {
    def imap[A, B](fa: F[A])(f: A => B)(g: B => A): F[B]
  }



  trait Functor[F[_]] extends Invariant[F] {
    def map[A, B](fa: F[A])(f: A => B): F[B]

    override def imap[A, B](fa: F[A])(f: A => B)(g: B => A): F[B] = map(fa)(f)
  }

  trait Contravariant[F[_]] extends Invariant[F] {
    def contramap[A, B](fa: F[A])(f: B => A): F[B]

    override def imap[A, B](fa: F[A])(f: A => B)(g: B => A): F[B] = contramap(fa)(g)
  }




  trait Semigroupal[F[_]] {
    def product[A, B](fa: F[A], fb: F[B]): F[(A, B)]
  }



  // "Apply" is omitted
  trait Applicative[F[_]] extends Functor[F] with Semigroupal[F] {
    def pure[A](value: A): F[A]
    def ap[A, B](ff: F[A => B])(fa: F[A]): F[B]

    override def product[A, B](fa: F[A], fb: F[B]): F[(A, B)] = ap(map(fa)(a => (b: B) => (a, b)))(fb)

    override def map[A, B](fa: F[A])(f: A => B): F[B] = ap(pure(f))(fa)
  }

  // "Divide" is omitted
  trait Divisible[F[_]] extends Contravariant[F] with Semigroupal[F] {
    def conquer[A]: F[A]

    def divide[A, B, C](fa: F[A], fb: F[B])(f: C => (A, B)): F[C] = contramap(product(fa, fb))(f)
  }



  trait Monad[F[_]] extends Applicative[F] {
    def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]
  }

  trait Comonad[F[_]] extends Divisible[F] {
    def coflatMap[A, B](fa: F[A])(f: B => F[A]): F[B]
  }
}

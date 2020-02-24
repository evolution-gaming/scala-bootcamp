package com.evolutiongaming.bootcamp.cats

import cats.Functor

object TypeHierarchy {

  /*
    Semigroup[A] {
      def combine(y: A): A
    }
   */

  /*
    Monoid[A] {
      def combine(y: A): A
    }

    def empty: A
   */


  /*
    Functor[F[A]] {
      def map(f: A => B): F[B]
    }
   */

  /*
    Applicative[F[A]] {
      def ap(f: F[A => B]): F[B]
    }

    def pure[A](x: A): F[A]
   */


  /*
    Monad[F[A]] {
      def flatMap(f: A => F[B]): F[B]
    }

    def pure[A](x: A): F[A]
   */


  // Laws
}

object Excercises {
  trait Applicative[F[_]] extends Functor[F] {
    def map[A,B](fa: F[A])(f: A => B): F[B]

    def unit[A](a: => A): F[A]

    // implement methods using other methods
    def map2[A,B,C](fa: F[A], fb: F[B])(f: (A, B) => C): F[C] = ???

    def apply[A,B](fab: F[A => B])(fa: F[A]): F[B] = ???

    def sequence[A](fas: List[F[A]]): F[List[A]] = ???

    def traverse[A,B](as: List[A])(f: A => F[B]): F[List[B]] = ???
  }

  trait Monad[M[_]] extends Functor[M] {
    def unit[A](a: => A): M[A]

    // implement methods using other methods
    def flatMap[A,B](ma: M[A])(f: A => M[B]): M[B] = ???

    def join[A](mma: M[M[A]]): M[A] = ???

    def map[A,B](ma: M[A])(f: A => B): M[B] = ???

    def map2[A,B,C](ma: M[A], mb: M[B])(f: (A, B) => C): M[C] = ???
  }
}

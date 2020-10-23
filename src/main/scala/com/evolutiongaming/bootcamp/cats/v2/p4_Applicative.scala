package com.evolutiongaming.bootcamp.cats.v2

object p4_Applicative {

  /**
    * Applicative defines a `pure` method that allows us to construct an instance of Applicative
    * In cats, Applicative is an ancestor of Apply type class, which defines an `ap` method.
    * The essence of the `ap` method is to apply a function f: A => B in to a value of type A in given context F[_]
    * We define a simpler trait here with `pure` method only to avoid unnecessary complication.
    * */
  trait EvoApplicative[F[_]] {
    def pure[A](x: A): Option[A]
  }

  /**
    * Ex 4.0 implement an EvoApplicative for Option
    * */
  val optionApplicative: EvoApplicative[Option] = new EvoApplicative[Option] {
    override def pure[A](x: A): Option[A] = ???
  }
}

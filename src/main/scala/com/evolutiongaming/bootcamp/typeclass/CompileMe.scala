package com.evolutiongaming.bootcamp.typeclass
import cats._
import cats.arrow.Profunctor

case class User(name: String, age: Int)

object ImplementMe {

  implicit val userEq: Eq[User] = null

  implicit val intSemigroup: Semigroup[Int] = null

  implicit val intMonoid: Monoid[Int] = null

  implicit val stringMonoid: Monoid[String] = null

  implicit val listSemigroup: SemigroupK[List] = null


  // https://github.com/typelevel/kind-projector

  implicit def mapFunctor[K]: Functor[Map[K, *]] = null

  implicit def eitherRightFunctor[L]: Functor[Either[L, *]] = null

  implicit def eitherLeftFunctor[R]: Functor[Either[*, R]] = null


  implicit val bifunctorEither: Bifunctor[Either] = null

  implicit def contravariantFunction[B]: Contravariant[* => B] = null

  implicit val profunctorFunction: Profunctor[Function1] = null

}

object CompileMe {

/*
  object Task1 {
    import cats.syntax.show._

    def prettyPrint[T: Show](e: T): String = s"~~~${e.show}~~~"

    val userShowInstance: Show[User] = (t: User) => s"User name=${t.name}"

    println(prettyPrint(User("John", 21)))
  }
*/

/*
  object Task2 {
    val users = List(User("John", 22), User("John", 23), User("John", 21))
    users.min
  }
*/

/*
  object Task3 {
    import scala.concurrent.Future
    Future { 3 }
  }
*/

}

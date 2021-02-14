package com.evolutiongaming.bootcamp.typeclass.practical

import com.evolutiongaming.bootcamp.typeclass.practical.Fp.{Jsonable, User}
import com.evolutiongaming.bootcamp.typeclass.practical.Summoner.Jsonable

case class Json(s: String) // my very basic json class

object Oop extends App {
  trait Jsonable {
    def toJson: Json
  }

  // my imaginary method which makes use of Jsonable
  def printBeautifully(x: Jsonable): Unit = {
    println(x.toJson)
  }

  // my entity
  final case class User(name: String) extends Jsonable {
    def toJson: Json = Json(s"{name: $name}") // Jsonable implementation for User
  }

  printBeautifully(User("Oleg"))
}

// the main idea behind typeclass is here
// ask questions if you can't understand what is going on
object Fp extends App {

  trait Jsonable[T] { // <- the typeclass itself: a trait with one type parameter
    //           ^
    //   the type parameter
    def toJson(entity: T): Json // it may have many methods but usually one
  }

  // a typeclass describes an interface which can be implemented for different types

  // my imaginary method changed its signature a bit
  def printBeautifully[A](x: A)(implicit jsonable: Jsonable[A]): Unit = {
    println(jsonable.toJson(x))
  }

  // my entity does not implement anything and can be implemented with no idea Jsonable exists
  final case class User(name: String)

  // here goes the implementation
  // we can define it not touching User or Jsonable or anything
  implicit val userJsonable: Jsonable[User] = new Jsonable[User] {
    def toJson(user: User): Json = Json(s"{name: ${user.name}")
  }

  // the usage is the same
  printBeautifully(User("Oleg"))
}

// lets add pieces of sugar one by one
// don't try to remember everything

object SingleAbstractMethod {
  // they are the same, choose any style you like

  implicit val was: Jsonable[User] = new Jsonable[User] {
    def toJson(user: User): Json = Json(s"{name: ${user.name}")
  }

  implicit val now: Jsonable[User] = user => Json(s"{name: ${user.name}")
}

object ContextBound {
  def printBeautifullyOld[A](x: A)(implicit jsonable: Jsonable[A]): Unit = {
    println(jsonable.toJson(x))
  }

  def printBeautifully[A: Jsonable](x: A): Unit = {
    val jsonable = implicitly[Jsonable[A]]
    println(jsonable.toJson(x))
  }
}

object Summoner {
  object Jsonable { // but it gets a companion object

    // with nice summon method (could have any name, apply for eg)
    def apply[F](implicit instance: Jsonable[F]): Jsonable[F] = instance
  }

  // so now we can change
  def printBeautifullyOld[A: Jsonable](x: A): Unit = {
    val jsonable = implicitly[Jsonable[A]]
    println(jsonable.toJson(x))
  }

  // to
  def printBeautifully[A: Jsonable](x: A): Unit = {
    println(Jsonable[A].toJson(x))
  }
}

object Syntax {

  object JsonableSyntax {

    implicit class JsonableOps[A](x: A) {
      def toJson(implicit j: Jsonable[A]): Json = {
        j.toJson(x)
      }
    }

  }

  // so now we can change
  def printBeautifullyOld[A: Jsonable](x: A): Unit = {
    println(Jsonable[A].toJson(x))
  }

  // to
  import JsonableSyntax._
  def printBeautifully[A: Jsonable](x: A): Unit = {
    println(x.toJson)
  }
}


object Result {

  // --- json library (provides the typeclass) ---
  trait Jsonable[T] {
    def toJson(entity: T): Json
  }

  object Jsonable {
    def apply[F](implicit instance: Jsonable[F]): Jsonable[F] = instance
  }

  object JsonableSyntax {

    implicit class JsonableOps[A](x: A) {
      def toJson(implicit j: Jsonable[A]): Json = {
        j.toJson(x)
      }
    }
  }

  // --- library which makes use of json (for example some http library) ---
  import JsonableSyntax._
  def printBeautifully[A: Jsonable](x: A): Unit = {
    println(x.toJson)
  }

  // --- domain library of your project ---
  case class User(name: String)

  // --- domain utils library ---
  implicit val JsonableUser: Jsonable[User] = user => Json(s"{name: ${user.name}") // good luck choosing name

  // --- you ---
  printBeautifully(User("Oleg"))
}

// having two implementations for the same type (like different ways to make json out of User) is possible
// but considered to be bad

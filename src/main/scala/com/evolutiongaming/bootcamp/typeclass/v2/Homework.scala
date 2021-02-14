package com.evolutiongaming.bootcamp.typeclass.v2

// make as many exercises as you can

object Task1 {
  final case class Money(amount: BigDecimal)

  // TODO: create Ordering instance for Money
  implicit val moneyOrdering: Ordering[Money] = ???
}

object Task2 {
  trait Show[T] { // fancy toString
    def show(entity: T): String
  }

  final case class User(id: String, name: String)

  // TODO: create Show instance for User

  // TODO: create syntax for Show so i can do User("1", "Oleg").show
}

object Task3 {
  type Error = String
  trait Parse[T] { // invent any format you want or it can be csv string
    def parse(entity: String): Either[Error, T]
  }

  final case class User(id: String, name: String)

  // TODO: create Parse instance for User

  // TODO: create syntax for Parse so i can do "lalala".parse[User] (and get an error because it is obviously not a User)
}

object Task4 {
  // TODO: design a typesafe equals so i can do a === b, but it won't compile if a and b are of different types
  // define the typeclass (think of a method signature)
  // remember `a method b` is `a.method(b)`
}

object AdvancedHomework {
  // TODO: create a typeclass for flatMap method
}

package com.evolutiongaming.bootcamp.implicits

import akka.cluster.client.protobuf.msg.ClusterClientMessages.Contacts

object ContextBounds {

  object Exercise1 {

    // Let's revisit some functions we implemented while studying implicit classes:
    def concat(a: Int, b: Int): Int = ???

    // Exercise 1: Implement repeat functions for various types.
    // For `Int` using `concat` function above. For others, use built-in methods.

    // `repeat(72, 3)` should return `727272`
    def repeat(a: Int, times: Int): Int = ???

    // `repeat("Scala", 3)` should return `"ScalaScalaScala"`
    def repeat(a: String, times: Int): String = ???

    // `repeat(List(10, 20, 30), 3)` should return `List(10, 20, 30, 10, 20, 30, 10, 20, 30)`
    def repeat(a: List[Int], times: Int): List[Int] = ???

  }

  // Do you find anything common in these methods? These methods, basically, do
  // exactly the same job: calling concatenation method of the specific type.
  //
  // In some of programming languages, this is the most we can achieve. But not
  // in Scala. In Scala we have type parameters.

  // Exercise 2: Implement repeat functions so it works for `Int`, `String` and `List[Int]`.
  object Exercise2 {
    def repeat[T](a: T, times: Int): T = ???
  }

  // (spoiler was deleted here, will be told by lecturer)

  // What if we had a univeral way to concatenate all of these types? Let's make
  // such a univeral way...
  //
  // Exercise 3:
  // - Implement `Concatenable` so it works for `Int`, `String` and `List[Int]`.
  // - Now implement `repeat` function using the new trait.

  trait Concatenable[T] {
    def concat(a: T, b: T): T
  }
  object Concatenable {
    val forInt: Concatenable[Int]           = new Concatenable[Int] {
      def concat(a: Int, b: Int): Int = ???
    }
    val forString: Concatenable[String]     = new Concatenable[String] {
      def concat(a: String, b: String): String = ???
    }
    val forListInt: Concatenable[List[Int]] = new Concatenable[List[Int]] {
      def concat(a: List[Int], b: List[Int]): List[Int] = ???
    }
  }

  object Exercise3 {
    def repeat[T](a: T, times: Int, concatenable: Concatenable[T]): T = ???
  }

  // Do you find this way convenient / readable? What else we could do to
  // improve it?

  // Exercise 4:
  // - Make the `repeat` method above more convenient to use by moving
  //   `Concatenable` to implicit parameter block.
  // - Make implicits for `Int`, `String` and `List[Int]` available by making
  //   the vals in `Concatenable` companion object implicit.
  object Exercise4 {
    // def repeat[T](...)(implicit ...): T = ???
  }

  // Now let's use our `repeat` method for something useful:
  // Exercise 5: implement the methods using `repeat` we just made.
  object Exercise5 {

    def repeatTenTimes[T](a: T)(implicit concatenable: Concatenable[T]): Unit                           = ???
    def repeatTenTimesIfTrue[T](condition: Boolean)(a: T)(implicit concatenable: Concatenable[T]): Unit = ???

  }

  // While we made `repeatTenTimes` quite nice, but we still have to write
  // `implicit` keyword and it does not look really tidy because we have
  // `concatenable` parameter name despite not using it anywhere.
  //
  // The pattern is so prevalent in Scala that there is a special syntax sugar
  // to simplify the encoding called "context bounds".
  //
  // The following two methods are exactly the same:
  def method1[T](a: T)(implicit concatenable: Concatenable[T]): Unit = ()
  def method2[T: Concatenable](a: T): Unit                           = ()

  // Exercise 6: Use context bound to tidy up `repeat` method above
  object Exercise6 {
    // def repeat[T: ...](...): T = ???
  }

  // Do you find this way convenient / readable? What else we could do to
  // improve it?
  //
  // We are still calling `repeat(argument, 7)` all the time instead of
  // `argument.repeat(7)`
  //
  // Turns out we can combine extension methods with implicit parameters
  // to create our own _syntax_.
  object Exercise7 {

    implicit class RepeatSyntax[T](a: T) {
      def repeat(times: Int)(implicit concatenable: Concatenable[T]): T = ???
    }

    // 72.repeat(3)
    // "Scala".repeat(3)
    // List(10, 20, 30).repeat(3)

    // Exercise 7: Use the same approach to create syntax for `concat`
    // I.e. this should compile:
    // 72.concat(651)

  }

}

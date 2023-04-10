package com.evolutiongaming.bootcamp.basics

object BasicsDescoped {
  // A more convoluted example:
  def formatNamedDouble(name: String, format: Double => String): Double => String = { x: Double =>
    s"$name = ${format(x)}"
  }

  val fourDecimalPlaces: Double => String = (x: Double) => f"$x%.4f"
  val formattedNamedDouble: String        = formatNamedDouble("x", fourDecimalPlaces)(Math.PI) // x = 3.1416

  // The function `formatNamedDouble` can be rewritten in a more general way as follows:

  def formatNamedValue[A](name: String, format: A => String): A => String = { x: A =>
    s"$name = ${format(x)}"
  }

  val commasForThousands: Long => String = (x: Long) => f"$x%,d"
  val formattedLong: String              = formatNamedValue("y", commasForThousands)(123456) // y = 123,456

  // Question: What is `A` for `formatNamedValue` in this `formattedLong` invocation of it?

  // Exercise. Invoke `formatNamedValue` with a `List[String]` as `A`. You can use `_.mkString(", ")` to
  // concatenate the list with comma as a delimiter. You can provide the `List[String]` type
  // explicitly after the method name or for the `format` function.

  // Homework. Implement functions that calculate https://en.wikipedia.org/wiki/Least_common_multiple and
  // https://en.wikipedia.org/wiki/Greatest_common_divisor for integers.

  def lcm(a: Int, b: Int): Int = ???
  def gcd(a: Int, b: Int): Int = ???

  // Create a new Git public repository for your homework solutions, use `basics` package for this homework.
  // You can use `sbt new scala/hello-world.g8` to start a new bare-bones Scala SBT project.
}

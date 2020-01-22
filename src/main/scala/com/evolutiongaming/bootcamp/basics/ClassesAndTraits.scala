package com.evolutiongaming.bootcamp.basics

object ClassesAndTraits {
  // You can follow your progress using the tests in `ClassesAndTraitsSpec`.

  // Classes in Scala are blueprints for creating objects. They can contain methods, values, variables,
  // types, objects, traits, and classes which are collectively called members.

  class MutablePoint(var x: Double, var y: Double) {
    def move(dx: Double, dy: Double): Unit = {
      x = x + dx
      y = y + dy
    }

    override def toString: String =
      s"($x, $y)"
  }

  val point1 = new MutablePoint(3, 4)
  println(point1.x) // 3.0
  println(point1)   // (3.0, 4.0)

  // Question. Is MutablePoint a good design? Why or why not?

  // Traits define a common interface that classes conform to. They are similar to Java's interfaces.
  // Classes and objects can extend traits but traits cannot be instantiated and therefore have no parameters.

  // Subtyping
  // Where a given trait is required, a subtype of the trait can be used instead.

  sealed trait Shape extends Located with Bounded

  sealed trait Located {
    def x: Double
    def y: Double
  }

  sealed trait Bounded {
    def minX: Double
    def maxX: Double
    def minY: Double
    def maxY: Double
  }

  final case class Point(x: Double, y: Double) extends Shape {
    override def minX: Double = x
    override def maxX: Double = x
    override def minY: Double = y
    override def maxY: Double = y
  }

  final case class Circle(centerX: Double, centerY: Double, radius: Double) extends Shape {
    override def x: Double = centerX
    override def y: Double = centerY
    override def minX: Double = centerX - radius
    override def maxX: Double = centerX + radius
    override def minY: Double = centerY - radius
    override def maxY: Double = centerY + radius
  }

  // Case Classes
  //
  // Case classes are like regular classes, but with extra features which make them good for modelling
  // immutable data. They have all the functionality of regular classes, but the compiler generates additional
  // code:
  // - Case class constructor parameters are public `val` fields, publicly accessible
  // - `apply` method is created in the companion object, so you don't need to use `new` to create a new
  //   instance of the class
  // - `unapply` method which allows you to use case classes in `match` expressions (pattern matching)
  // - a `copy` method is generated
  // - `equals` and `hashCode` methods are generated, which let you compare objects & use them in collections
  // - `toString` method is created for easier debugging purposes

  val point2 = Point(1, 2)
  println(point2.x)

  val shape: Shape = point2
  val point2Description = shape match {
    case Point(x, y)  => s"x = $x, y = $y"
    case _            => "other shape"
  }

  val point3 = point2.copy(x = 3)
  println(point3.toString) // Point(3, 2)

  // Exercise. Implement an algorithm for finding the minimum bounding rectangle
  // (https://en.wikipedia.org/wiki/Minimum_bounding_rectangle) for a set of `Bounded` objects.
  //
  def minimumBoundingRectangle(objects: Set[Bounded]): Bounded = {
    new Bounded {
      implicit private val doubleOrdering: Ordering[Double] = Ordering.Double.IeeeOrdering

      // if needed, fix the code to be correct
      override def minX: Double = objects.map(_.minX).min
      override def maxX: Double = objects.map(_.minX).min
      override def minY: Double = objects.map(_.minX).min
      override def maxY: Double = objects.map(_.minX).min
    }
  }

  // Pattern matching and exhaustiveness checking
  def describe(x: Shape): String = x match {
    case Point(x, y) => s"Point(x = $x, y = $y)"
    case Circle(centerX, centerY, radius) => s"Circle(centerX = $centerX, centerY = $centerY, radius = $radius)"
    case Rectangle(x1, y1, x2, y2) => s"Rectangle, bottom point: ($x1, $y1), top point: ($x2, $y2)"
  }

  // Exercise. Add another Shape class called Rectangle and check that the compiler catches that we are
  // missing code to handle it in `describe`.

  case class Rectangle(xLeftBot: Double, yLeftBot: Double, xRightTop: Double, yRightTop: Double) extends Shape {
    override def x: Double = xRightTop - xLeftBot
    override def y: Double = yRightTop - yLeftBot
    override def minX: Double = xLeftBot
    override def maxX: Double = xRightTop
    override def minY: Double = yLeftBot
    override def maxY: Double = yRightTop
  }

  // Exercise. Change the implementation of `minimumBoundingRectangle` to return a `Rectangle` instance.
  // What are the pros & cons of each implementation?

  def minimumBoundingRectangle(objects: Set[Bounded]): Rectangle = {
    implicit val doubleOrdering: Ordering[Double] = Ordering.Double.IeeeOrdering
    Rectangle(objects.map(_.minX).min, objects.map(_.minY).min, objects.map(_.maxX).max, objects.map(_.maxY).max)
  }

  // Exercise. The tests for `minimumBoundingRectangle` in `ClassesAndTraitsSpec` are insufficient.
  // Improve the tests.

  // Generic classes and type parameters

  // In a similar way as we saw with polymorphic methods, classes and traits can also take type parameters.
  // For example, you can define a Stack[A] which works with any type of element A.
  final case class Stack[A](elements: List[A] = Nil) {
    def push(x: A): Stack[A] = Stack(x :: elements)
    def peek: Option[A] = elements.headOption
    def pop: Option[(A, Stack[A])] = peek map { x =>
      (x, Stack(elements.tail))
    }
  }
}

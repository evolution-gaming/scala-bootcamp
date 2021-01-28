package com.evolutiongaming.bootcamp.basics

object ClassesAndTraits {
  // You can follow your progress using the tests in `ClassesAndTraitsSpec`:
  //   sbt "testOnly com.evolutiongaming.bootcamp.basics.ClassesAndTraitsSpec"

  // Classes in Scala are blueprints for creating object instances. They can contain methods, values,
  // variables, types, objects, traits, and classes which are collectively called members.

  class MutablePoint(var x: Double, var y: Double) {
    def move(dx: Double, dy: Double): Unit = {
      x = x + dx
      y = y + dy
    }

    override def toString: String =
      s"($x, $y)"
  }

  // Singleton objects are defined using `object`.
  // It is a class that has exactly one instance.
  // An object with the same name as a class is called a companion object.
  //
  // Use it to contain methods and values related to this trait or class, but that aren't
  // specific to instances of this trait or class.
  object MutablePoint {
    def apply(x: Double, y: Double): MutablePoint = new MutablePoint(x, y)

    def unapply(point: MutablePoint): Option[(Double, Double)] = Option((point.x, point.y))
  }

  val point1 = new MutablePoint(3, 4)
  println(point1.x) // 3.0
  println(point1) // (3.0, 4.0)

  // Question. Is MutablePoint a good design? Why or why not?

  // Traits define a common interface that classes conform to. They are similar to Java's interfaces.

  // A trait can be thought of as a contract that defines the capabilities and behaviour of a component.

  // Subtyping
  // Where a given trait is required, a subtype of the trait can be used instead.

  // Classes and singleton objects can extend traits.
  //
  // This allows "programming to the interface" approach where you depend on traits instead of their
  // specific implementations (classes or objects).
  //
  // This makes code more reusable and testable.

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
    override def x: Double    = ???
    override def y: Double    = ???
    override def minX: Double = ???
    override def maxX: Double = ???
    override def minY: Double = ???
    override def maxY: Double = ???
  }

  // Case Classes
  //
  // Case classes are like regular classes, but with extra features which make them good for modelling
  // immutable data. They have all the functionality of regular classes, but the compiler generates additional
  // code, such as:
  // - Case class constructor parameters are public `val` fields, publicly accessible
  // - `apply` method is created in the companion object, so you don't need to use `new` to create a new
  //   instance of the class
  // - `unapply` method which allows you to use case classes in `match` expressions (pattern matching)
  // - a `copy` method is generated
  // - `equals` and `hashCode` methods are generated, which let you compare objects & use them in collections
  // - `toString` method is created for easier debugging purposes
  // - extends Serializable
  //
  // Case object provides default equals, hashCode, unapply, toString methods, extends Serializable
  //
  // When declaring a case class, make it final. Otherwise someone may decide to inherit from it
  // case-to-case inheritance is prohibited

  val point2 = Point(1, 2)
  println(point2.x)

  val shape: Shape = point2
  val point2Description = shape match {
    case Point(x, y) => s"x = $x, y = $y"
    case _           => "other shape"
  }

  val point3 = point2.copy(x = 3)
  println(point3.toString) // Point(3, 2)

  // Exercise. Implement an algorithm for finding the minimum bounding rectangle
  // (https://en.wikipedia.org/wiki/Minimum_bounding_rectangle) for a set of `Bounded` objects.
  //
  def minimumBoundingRectangle(objects: Set[Bounded]): Bounded =
    new Bounded {
      implicit private val doubleOrdering: Ordering[Double] = Ordering.Double.IeeeOrdering

      // if needed, fix the code to be correct
      override def minX: Double = objects.map(_.minX).min
      override def maxX: Double = objects.map(_.minX).min
      override def minY: Double = objects.map(_.minX).min
      override def maxY: Double = objects.map(_.minX).min
    }

  // Pattern matching and exhaustiveness checking
  def describe(x: Shape): String = x match {
    case Point(x, y)                      => s"Point(x = $x, y = $y)"
    case Circle(centerX, centerY, radius) => s"Circle(centerX = $centerX, centerY = $centerY, radius = $radius)"
  }

  // Singleton can extend classes and mix in traits
  object Origin extends Located {
    override def x: Double = 0
    override def y: Double = 0
  }

  object Bounded {
    def minimumBoundingRectangle(objects: Set[Bounded]): Bounded = ???
  }

  // Exercise. Add another Shape class called Rectangle and check that the compiler catches that we are
  // missing code to handle it in `describe`.

  // Let us come back to our `Shape`-s and add a `Movable` trait
  // which will have a method:
  //
  //   def move(dx: Double, dy: Double)
  //
  // How should we implement `move` for various types?
  //
  // What should be the return type of the `move` method in `Shape`? In `Point` and other sub-types?

  // Generic classes and type parameters

  // In a similar way as we saw with polymorphic methods, classes and traits can also take type parameters.
  // For example, you can define a `Stack[A]` which works with any type of element `A`.

  // Question. Do you agree with how the stack is modelled here? What would you do differently?
  final case class Stack[A](elements: List[A] = Nil) {
    def push(x: A): Stack[A] = ???
    def peek: A              = ???
    def pop: (A, Stack[A])   = ???
  }

  // Homework
  //
  // Add additional 2D shapes such as triangle and square.
  //
  // In addition to the 2D shapes classes, add also 3D shapes classes
  // (origin, point, sphere, cube, cuboid, 3D triangle - you can add
  // others if you think they are a good fit).
  //
  // Add method `area` to 2D shapes.
  //
  // Add methods `surfaceArea` and `volume` to 3D shapes.
  //
  // If some of the implementation involves advanced math, it is OK
  // to skip it (leave unimplemented), the primary intent of this
  // exercise is modelling using case classes and traits, and not math.
}

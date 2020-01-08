package com.evolutiongaming.bootcamp.basics

object Basics {
  /*
    Functions in Scala have a name, value parameters (with types), a return type (can be skipped) and a
    function body.

    def functionName(parameter1: Type1, parameter2: Type2): ReturnType = {
       // code goes here
    }

    We're ignoring type parameters for now and will get to them later.
  */

  // Exercise. Define a function "hello" which returns a String "Hello, <name>!" where '<name>' is the
  // provided String parameter 'name'.
  //
  // Note that this can be done using both String concatenation and interpolation.
  //
  // `???` can be used to indicate code that is yet to be implemented.
  def hello(name: String): String = ???

  // Exercise. Define a function "add" which takes two integers and returns their sum.
  def add(a: Int, b: Int): Int = a * 42

  // Values vs Variables in Scala
  // You declare variables using `var`:
  var int1 = 4
  int1 = 5 // you can later reassign them (assign a different value to this variable)
  // A variable is a mutable, typed storage unit. A variable can be assigned data when it is defined and can
  // also be reassigned data at any time.

  // You declare values (constant or immutable variables) using `val` in Scala:
  val int2 = 6
  // A value is an immutable, typed storage unit. A value can be assigned data when it is defined, but can
  // never be reassigned.

  // Immutability is a good thing and leads to code which is easier to maintain.
  // Prefer `val` to `var` except in cases where `var` cannot be avoided.

  // Types can be explicitly specified ...
  val int3: Int = 7
  // ... or inferred by the compiler ...
  val int4 = 3

  // Types define the data that a value can contain.
  //
  // Types help catch errors in compile time. Assigning an `Int` value to `String` value will result in a
  // compile time error. Try it:
  //
  // val e: String = b // uncomment this line
  //
  // Types can be thought of as defining a set of all possible values that a particular value can be.
  //
  // For example, a value having a type `Boolean` means it can only be one of two values - `true` and `false`.

  val bool1: Boolean = true
  val bool2: Boolean = false

  // Exercise. List all boolean values (here and onwards for these exercises ignoring `null`).
  val AllBooleans: Set[Boolean] = Set( /* add values here, separated by commas */ )

  // TODO: String-s, String literals - single-line, multi-line, interpolation
  // TODO: Double-s & Float-s

  // For consideration. Can we make a `Set` with all possible `Double` or `String` values?

  // Primitive vs Non-Primitive types

  // TODO: list all primitive types, explain the difference

  // Due to the need to be compatible with legacy Java code, a special value `null` can also be assigned
  // to non-primitive values:
  val nullString: String = null

  // You shouldn't do this and should avoid `null` in Scala code. `null`-s are error-prone and lead to
  // unexpected NullPointerExceptions. We will see later how to handle `null`-s safely.

  // TODO: Mutable vs Immutable collections
  // TODO: Most commonly used collections - List-s, Vector-s, Set-s, Map-s
  // TODO: Tuples
}

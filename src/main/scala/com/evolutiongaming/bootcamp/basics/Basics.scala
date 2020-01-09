package com.evolutiongaming.bootcamp.basics

object Basics {
  // Let's start by quickly going through the basic building blocks of Scala programs.
  // You can follow your progress using the tests in `BasicsSpec`.

  // Values

  // A value is an immutable, typed storage unit. A value can be assigned data when it is defined, but can
  // never be reassigned.

  // You declare values (constant or immutable variables) using `val`:
  val int1 = 4

  // A variable is a mutable, typed storage unit. A variable can be assigned data when it is defined and can
  // also be reassigned data at any time.

  // Variables

  // You declare variables using `var`:
  var int2 = 5
  int2 = 6 // you can later reassign them (assign a different value to this variable)

  // Immutability is a good thing and leads to code which is easier to reason about and thus maintain.
  // Prefer `val` to `var` except in cases where `var` cannot be avoided.

  // Types define the data that a value can contain.

  // Types can be explicitly specified ...
  val int3: Int = 7
  // ... or inferred by the compiler ...
  val int4 = 3

  // The Scala type system is rich and powerful and this section will discuss only the basics of it.

  // Types help catch errors in compile time. Assigning an `Int` value to `String` value will result in a
  // compile time error. Try it:
  //
  // val string1: String = b // uncomment this line
  //
  // Types can be thought of as defining a set of all possible values that a particular value can be.
  //
  // For example, a value having a type `Boolean` means it can only be one of two values - `true` and `false`.

  val bool1: Boolean = true
  val bool2: Boolean = false

  // Exercise. List all boolean values
  val allBooleans: Set[Boolean] = Set( /* add values here, separated by commas */ )

  /* Common boolean operations:
      !false         // true - `!` is negation
      !true          // false
      false == true  // false - boolean comparison
      true && false  // false - logical `and`
      true || false  // true - logical `or`
   */

  // Byte - 8-bit signed integer (-2^7 to 2^7 - 1, inclusive), -128 to 127
  val byte1: Byte = 4

  // Question. How large would a set of all possible Byte values be?

  // Short - 16-bit signed integer (-2^15 to 2^15 - 1, inclusive)
  // 32,768 to 32,767
  val short1: Short = 5

  // Int - 32-bit integer (-2^31 to 2^31 - 1, inclusive)
  // 2,147,483,648 to 2,147,483,647
  val int5: Int = Int.MaxValue

  // Long - 64-bit integer (-2^63 to 2^63 - 1, inclusive)
  // -9,223,372,036,854,775,808 to +9,223,372,036,854,775,807
  val long1: Long = Long.MinValue

  // Float - 32-bit single-precision float
  // 1.40129846432481707e-45 to 3.40282346638528860e+38 (positive or negative)
  // Float.NaN is a special "not a number" float as per https://en.wikipedia.org/wiki/NaN and
  // IEEE 754 standard
  val float1: Float = Float.NaN

  // Double - 64-bit double-precision float
  // 4.94065645841246544e-324d to 1.79769313486231570e+308d (positive or negative)
  val double1: Double = Double.PositiveInfinity

  /* Common numeric operations:
      1 + 2   // 3
      2 - 1   // 1
      2 * 3   // 6
      6 / 3   // 2
      6 / 4   // 1
      6.0 / 4 // 1.5
      6 / 4.0 // 1.5
      6 % 5   // 1 - "remainder" or "modulo"
   */

  // Char - 16-bit unsigned Unicode character (0 to 2^16 - 1, inclusive)
  // 0 to 65,535
  val char1: Char = 'λ'

  // String - a sequence of characters
  val string1: String = "αβγ"

  // Strings can be multi-line:
  val string2 =
    """
       Strings
       can
       be
       multiline.
    """
  // ... though beware of what this means for whitespace

  // String interpolation can be used to place values into Strings:
  val string3 = s"Value of string1 is $string1 while (byte1 + short1) is ${byte1 + short1}"
  val string4 = f"Formatted strings: ${Math.PI}%.4f" // Formatted strings: 3.1416

  /* Common string operations:
    "test".length                     // 4
    "test".take(2)                    // te
    "test".drop(2)                    // st
    "concatenate" + " " + "strings"   // concatenate strings
    "test".replace("te", "rr")        // rrst
    "test".indexOf('e')               // 1
    "test".indexOf('z')               // -1
    "test".indexOf("st")              // 2
    "test" * 3                        // testtesttest
   */

  // Equality
  val string5 = "abcc"
  val string6 = "ab" + "c" * 2

  string5 == string6 // true - equality comparison, uses the `equals` method
  string5 eq string6 // false - reference equality - are they the same object?

  // Note that this is different than Java.
  // Other ways of comparing values are also common, e.g., https://typelevel.org/cats/typeclasses/eq.html.

  /* Common comparison operations:
      4 == 4    // true
      4 != 4.0  // false
      5 > 5     // false
      4 < 5     // true
      5 >= 5    // true
      5 <= 3    // false
   */

  // Unit is a special type with only one possible value - `()`
  val unit1: Unit = ()
  val allUnitValues: Set[Unit] = Set(())
  // Unit can be thought of as a Java `void` equivalent.

  // Nothing is a special type with no possible values
  val allNothingValues: Set[Nothing] = Set()

  // Null
  //
  // Due to the need to be compatible with legacy Java code, a special value `null` can also be assigned
  // to non-primitive values:
  val nullString: String = null

  // You shouldn't do this and should avoid `null` in Scala code, instead preferring `Option` or other
  // more type-safe ways of indicating an absence of value.
  //
  // `null`-s are error-prone and lead to
  // unexpected NullPointerExceptions.

  // There is a proposal for Scala 3 to improve `null` handling:
  // https://contributors.scala-lang.org/t/sip-public-review-explicit-nulls/3889

  // Blocks and Expressions

  // Expressions are computable statements
  val expressionResult = 1 + 2 // == 3

  // You can combine expressions into blocks by surrounding them with `{` and `}`.
  // The result of the last expression in the block is the result of the block:

  val blockResult = {
    val x = 1 + 2
    x * 2
  } // == 6

  // Functions and Methods
  //
  // Function and methods are both blocks of code which take arguments and return a return value.
  //
  // While there are differences between functions and methods (functions can be thought of as a value, and a
  // method always has an associated class for it), these differences are not very relevant at this point
  // and for the time being we will often conflate these terms.
  //
  // Methods are defined using the `def` keyword and have a name, value parameters (with types), a return
  // type (can be skipped so that the compiler infers it) and a function body (implementation).
  //
  // def functionName(parameter1: Parameter1Type, parameter2: Parameter2Type): ReturnType = {
  //   // code goes here
  // }
  //
  // We're ignoring methods having multiple parameter lists or type parameters for now and will get to them
  // later.

  // Exercise. Define a method "helloMethod" which returns a String "Hello, <name>!" where '<name>' is the
  // provided String parameter 'name'.
  //
  // Try defining it using both String concatenation and interpolation.
  //
  // Note. `???` can be used to indicate code that is yet to be implemented.
  def helloMethod(name: String): String = ???

  // Exercise. Define a method "add" which takes two integers and returns their sum.
  def add(a: Int, b: Int): Int = a * 42 - b / 4 // replace with a correct implementation

  // You can use parameter names to specify them in a different order
  val sum1 = add(b = 2, a = 3) // addition is commutative though so it doesn't change the result

  // Methods can have default parameters
  def addNTimes(x: Int, y: Int, times: Int = 1): Int = x + y * times
  val sum2 = addNTimes(2, 3) // 5
  val sum3 = addNTimes(2, 3, 4) // 14

  // Functions are defined with the following syntax:
  //
  // val functionName: (Parameter1Type, Parameter2Type => ReturnType) = (parameter1: Parameter1Type, parameter2: Parameter2Type) => {
  //  // implementation code goes here
  // }
  //
  // Note that the `: (Parameter1Type, Parameter2Type => ReturnType)` part is the type annotation and can
  // often be skipped as it is inferred by the compiler.
  //
  // Exercise. Implement `helloFunction` using `helloMethod` you implemented above. Why was the type
  // annotation skipped when defining `helloFunction`?

  val helloFunction: String => String = (name: String) => name

  // If each argument of a function is used exactly once, you can use `_` to refer to them
  val addFunction: (Int, Int) => Int = _ + _

  // A static `main` returning `Unit` with a single `Array[String]` parameter is the
  // entry point to a Scala application (similar to Java, as required by the JDK).
  def main(args: Array[String]): Unit = {
    // You can print to the console using `print` and `println` methods
    print("One")
    println("Two")

    // Since printing using `print` and `println` isn't easily testable, you should refrain from using it
    // except for temporary debugging purposes.
  }

  // You won't always have to provide such a `main` method as some libraries will provide it for you, e.g.:
  // https://typelevel.org/cats-effect/datatypes/ioapp.html

  // Methods return the last expression from their implementation block.
  // While there is a `return` keyword which can do an early return it leads to code which is more difficult
  // to reason about and should be avoided.

  // Higher order functions
  //
  // Functions are first class citizens and can be passed as parameters to other functions, as well as
  // returned as return values from functions.

  // Example of a function returned as a return value:
  def greeter(intro: String): String => String = { name: String =>
    s"$intro, $name!"
  }

  val hello: String => String = greeter("Hello")
  val helloWorld: String = hello("World") // Hello, World!

  val goodMorning: String => String = greeter("Good morning")
  val goodMorningWorld: String = goodMorning("World") // Good morning, World!

  // A more convoluted example:
  def formatNamedDoubleValue(name: String, format: Double => String): Double => String = { x: Double =>
    s"$name = ${format(x)}"
  }

  val fourDecimalPlaces: Double => String = (x: Double) => f"$x%.4f"
  val formattedNamedDouble: String = formatNamedDoubleValue("x", fourDecimalPlaces)(Math.PI) // x = 3.1416

  // TODO: Exercise about higher order functions

  // Tuples
  //
  // A tuple is a value that contains a fixed number of elements, each with a distinct type. Tuples are immutable.

  val tuple1: (String, Double) = ("Pepper", 4.5)
  val tuple2: (String, Double, Int) = ("Onions", 2.24, 16)

  // Tuples are sometimes useful but should not be over-used as their elements aren't named.
  // Tuple elements can be accessed using `._1`, `._2` and similar, for example:
  val pepper1 = tuple1._1
  val pepperPrice1 = tuple1._2

  // However this should be done sparingly and instead preferring the following form:
  val (pepper3, pepperPrice3) = tuple1

  // If you only need one of these values you can omit the other using `_`:
  val (pepper2, _) = tuple1

  // Option
  //
  // Option is a container which represents optional values. Instances of Option are either the object
  // None or an instance of Some containing a value.

  val nameUnknown: Option[String] = None
  val nameKnown: Option[String] = Some("Name")

  // Either
  //
  // Either represents a value of one of two possible types. Instances of Either are either an instance of
  // Left or Right.
  //
  // Commonly, Left is used to indicate an error while Right to indicate a normal execution.

  val errorOccurred: Either[String, Int] = Left("Failed to parse")
  val normalExecution: Either[String, Int] = Right(4)

  // More exercises to help internalise the "types define the set of possible values that a value can have":

  // Exercise. List all values of the type `Option[Boolean]`:
  val allOptionBooleans: Set[Option[Boolean]] = Set()

  // Exercise. List all values of the type `Either[Unit, Boolean]`:
  val allEitherUnitBooleans: Set[Either[Unit, Boolean]] = Set()

  // Exercise. List all values of the type `Either[Boolean, Boolean]`:
  val allEitherBooleanBooleans: Set[Either[Boolean, Boolean]] = Set()

  // Exercise. List all values of the type `(Boolean, Boolean)`:
  val allTupleBooleanBooleans: Set[(Boolean, Boolean)] = Set()

  // Question. Can we make a `Set` with all possible `Byte` values? `Double` values? `String` values?
}

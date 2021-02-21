package com.evolutiongaming.bootcamp.basics

object Basics {
  // Let's start by quickly going through the basic building blocks of Scala programs.

  // You can follow your progress using the tests in `BasicsSpec`.
  // You can run those tests from the IDE or using `sbt "testOnly com.evolutiongaming.bootcamp.basics.BasicsSpec"`.

  // If you are used to using REPL on other platforms, you can use this file in Scala REPL using
  // `sbt console` and then `import com.evolutiongaming.bootcamp.basics.Basics._`.
  // `sbt consoleQuick` will start REPL without compilation.

  // Values

  // A value is an immutable, typed storage unit. A value is assigned data when it is defined, but can
  // never be reassigned.

  // You declare values (constant or immutable variables) using `val`:
  val int1 = 4

  // val int1 = 5 <-- this doesn't work, values are immutable (cannot be reassigned)

  // Variables

  // A variable is a mutable, typed storage unit. A variable can be assigned data when it is defined and can
  // also be reassigned data at any time.

  // You declare variables using `var`:
  var int2 = 5
  int2 = 6 // you can later reassign them (assign a different value to this variable)

  // Immutability is a good thing and leads to code which is easier to reason about and thus maintain.

  // Prefer `val` to `var` except in cases where `var` cannot be avoided. As we progress with this course,
  // we will learn to avoid using `var`.

  // Types define the data that a value can contain.

  // Types can be explicitly specified ...
  val int3: Int = 7
  // ... or inferred by the compiler ...
  val int4 = 3

  // Your IDE has features that "Add type annotation to value definition", show "Type Info" and others.

  // The Scala type system is rich and powerful and this section will discuss only the basics of it.

  // Types help catch errors in compile time. Assigning an `Int` value to `String` value will result in a
  // compile time error. Try it:
  //
  // val string0: String = int1 // uncomment this line
  //
  // Types can be thought of as defining a set of all possible values that a particular value can be.
  //
  // For example, a value having a type `Boolean` means it can only be one of two values - `true` and `false`.

  val bool1: Boolean = true
  val bool2: Boolean = false

  // Exercise. List all boolean values.
  val allBooleans: Set[Boolean] = Set( /* add values here, separated by commas */ )

  /* Common boolean operations:
      !false          // true - `!` is negation
      !true           // false
      false == true   // false - boolean comparison
      true  && false  // false - logical `and`
      true  || false  // true - logical `or`
   */

  // Byte - 8-bit signed integer (-2^7 to 2^7 - 1, inclusive)
  // -128 to 127
  val byte1: Byte = 4

  // Question. How large would a set of all possible Byte values be?

  // Short - 16-bit signed integer (-2^15 to 2^15 - 1, inclusive)
  // -32,768 to 32,767
  val short1: Short = 5

  // Int - 32-bit integer (-2^31 to 2^31 - 1, inclusive)
  // -2,147,483,648 to 2,147,483,647
  val int5: Int = Int.MaxValue

  // Long - 64-bit integer (-2^63 to 2^63 - 1, inclusive)
  // -9,223,372,036,854,775,808 to 9,223,372,036,854,775,807
  val long1: Long = Long.MinValue

  // Float - 32-bit single-precision float
  // 1.40129846432481707e-45 to 3.40282346638528860e+38 (positive or negative)
  // Float.NaN is a special "not a number" float as per https://en.wikipedia.org/wiki/NaN and
  // IEEE 754 standard
  val float1: Float = Float.NaN

  // Question. What is the value of the following line?
  val floatComparisonResult: Boolean = 0.3 == 0.1 + 0.1 + 0.1

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
  val string2: String =
    """
       Strings
       can
       be
       multi-line.
    """
  // ... though beware of what this means for whitespace

  val string3: String =
    """
      |.stripMargin helps strip
      |extra whitespace from
      |multi-line strings.
      |""".stripMargin

  // String interpolation can be used to place values into Strings:
  val string4 = s"Value of string1 is $string1 while (byte1 + short1) is ${byte1 + short1}"
  val string5 = f"Formatted strings: ${Math.PI}%.4f" // Formatted strings: 3.1416

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
  val string6 = "abcc"
  val string7 = "ab" + "c" * 2

  string6 == string7 // true - equality comparison, uses the `equals` method
  string6 eq string7 // false - reference equality - are they the same object?

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

  // Where `void` is used in Java to indicate that a method doesn't return a value,
  // in Scala `Unit` is usually used.

  // Nothing is a special type with no possible values
  val allNothingValues: Set[Nothing] = Set()

  // Null
  //
  // Due to the need to be compatible with legacy Java code, a special value `null` can also be assigned
  // to non-primitive values:
  val nullString: String = null // you can also use `_` to assign the default value

  // You shouldn't do this and should avoid using `null` in Scala code, instead preferring `Option` or other
  // more type-safe ways of indicating an absence of value. We will learn about these in future lessons.

  // `null`-s are error-prone and lead to unexpected NullPointerExceptions.

  def artificialExample: String = if (System.getenv("test").toLowerCase == "value") "found" else "not found"

  // There is a proposal for Scala 3 to improve `null` handling:
  // https://contributors.scala-lang.org/t/sip-public-review-explicit-nulls/3889

  // There are nine predefined types which are non-nullable (also called primitive): Boolean, Byte, Short,
  // Int, Long, Float, Double, Char, Unit.

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
  // Function and methods are both units of code which take arguments and return a value.
  //
  // While there are differences between functions and methods (a function can be thought of as a value, and a
  // method always has an associated class for it), these differences are not very relevant at this point
  // and for the time being we will often conflate these terms.
  //
  // Methods are defined using the `def` keyword and have a name, value parameters (with types), a return
  // type (can be skipped so that the compiler infers it) and a method body (implementation).
  //
  // def methodName(parameter1: Parameter1Type, parameter2: Parameter2Type): ReturnType = {
  //   // code goes here
  // }
  //
  // For now we are ignoring  the distinction between pure functions (without side effects) vs impure
  // functions (with side effects), methods having multiple parameter lists or type parameters.

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
  val sum2 = addNTimes(2, 3) // 5, because 2 + 3 * 1
  val sum3 = addNTimes(2, 3, 4) // 14, because 2 + 3 * 4

  // Functions are defined with the following syntax:
  //
  // val functionName: (Parameter1Type, Parameter2Type) => ReturnType = (parameter1: Parameter1Type, parameter2: Parameter2Type) => {
  //  // implementation code goes here
  // }
  //
  // Note that the `: (Parameter1Type, Parameter2Type) => ReturnType` part is the type annotation and can
  // often be skipped as it is inferred by the compiler.
  //
  // Exercise. Implement `helloFunction` using `helloMethod` you implemented above. Why was the type
  // annotation skipped when defining `helloFunction`?

  val helloFunction: String => String = (name: String) => /* implement here */ name

  // Exercise. Using the aforementioned String `length` implement a `stringLength` function which returns
  // the length of the String passed.
  val stringLength: String => Int = (s: String) => /* implement here */ s.hashCode()

  // If each argument of a function is used exactly once, you can use `_` to refer to them
  val addFunction: (Int, Int) => Int = _ + _

  // First occurrence of _ - it's 1st argument.
  // Second occurrence of _ - it's 2nd argument.
  // And etc...

  // addFunction can be rewritten as:
  val addFunctionExpanded: (Int, Int) => Int = (x, y) => x + y

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
  def formatNamedDouble(name: String, format: Double => String): Double => String = { x: Double =>
    s"$name = ${format(x)}"
  }

  val fourDecimalPlaces: Double => String = (x: Double) => f"$x%.4f"
  val formattedNamedDouble: String = formatNamedDouble("x", fourDecimalPlaces)(Math.PI) // x = 3.1416

  // Exercise. Implement `power` method which takes a Byte `n` and returns a function from Int to
  // Long, raising the Int parameter provided to the n-th power using `Math.pow`.
  // For conversions, use `Double#round` (for rounding Double-s to Long-s) as well as `Byte` and `Int`
  // `toDouble` (for converting Byte-s and Int-s to Double-s).

  def power(n: Byte): Int => Long = { x: Int =>
    // implement here
    (x + n).toLong
  }

  // Polymorphic methods, or methods which take type parameters
  //
  // Methods in Scala can be parameterised by types of their arguments and return values. Type parameters are
  // enclosed in square brackets (in contrast with value parameters which are enclosed in parentheses).
  //
  // The function `formatNamedDouble` can be rewritten in a more general way as follows:

  def formatNamedValue[A](name: String, format: A => String): A => String = { x : A =>
    s"$name = ${format(x)}"
  }

  // Using such "parametric polymorphism" helps us do "parametric reasoning" - to reason about implementation
  // merely by looking at type signatures.

  // Using type parameters hides information from the implementation of the function. Hiding information
  // reduces the number of possible implementations, which makes code easier to understand and reuse.

  // Thus, while initially parametric polymorphisms seems to make our code more complicated, as you gain
  // experience with it, it will often help you write simpler, more maintainable code.

  val commasForThousands: Long => String = (x: Long) => f"$x%,d"
  val formattedLong: String = formatNamedValue("y", commasForThousands)(123456) // y = 123,456

  // Question: What is `A` for `formatNamedValue` in this `formattedLong` invocation of it?

  // Exercise. Invoke `formatNamedValue` with a `List[String]` as `A`. You can use `_.mkString(", ")` to
  // concatenate the list with comma as a delimiter. You can provide the `List[String]` type
  // explicitly after the method name or for the `format` function.

  // Tuples
  //
  // A tuple is a value that contains a fixed number of elements, each with a distinct type. Tuples are immutable.

  val tuple1: (String, Double) = ("Pepper", 4.5)
  val tuple2: (String, Double, Int) = ("Onions", 2.24, 16)

  // Tuples are sometimes useful but should not be over-used as their elements aren't named.
  // Tuple elements can be accessed using `._1`, `._2` and similar, for example:
  val pepper1 = tuple1._1
  val pepperPrice1 = tuple1._2

  // However this should be done sparingly and instead preferring the following destructuring form:
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

  // Homework. Implement functions that calculate https://en.wikipedia.org/wiki/Least_common_multiple and
  // https://en.wikipedia.org/wiki/Greatest_common_divisor for integers.

  def lcm(a: Int, b: Int): Int = ???
  def gcd(a: Int, b: Int): Int = ???

  // Create a new Git public repository for your homework solutions, use `basics` package for this homework.
  // You can use `sbt new scala/hello-world.g8` to start a new bare-bones Scala SBT project.
}

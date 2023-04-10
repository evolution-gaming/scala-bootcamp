package com.evolutiongaming.bootcamp.implicits

import java.time.Instant

// *Implicits*
//
// Implicits used to be one of the most controversial topics in Scala language.
// From one hand, it had a reputation of obscure and hard to follow feature,
// from the other hand, it enabled a tremendous power to the developers and
// plays a big part in Scala success as a language and as a platform.
//
// My own opinion about the topic is that implicits are acutally quite simple,
// but are not well explained and therefore made sound more complex than these
// are in reality.
//
// The good example is Scala Language Specification, which is an awesome
// document describing the topic precisely up the little details, but, to my
// mind, too complicated for a person new to the language:
// https://scala-lang.org/files/archive/spec/2.13/07-implicits.html
//
// One of the issues is that implicits are actually a set of unrelated (or
// weakly related) features of Scala language having the same name.
//
// The issue is widely recognized, so Scala 3 has reworked the area heavily,
// keepig the concept but giving more useful names:
// https://docs.scala-lang.org/scala3/reference/contextual.html
//
// This lecture will try to expose the myth and explain implicits as they are:
// a set of really simple, but powerful concepts that allow Scala to be a really
// _scalable_ and powerful programming language.
//
object ImplicitClasses {

  // *Implicit classes (extensions methods in Scala 3)*
  //
  // One of the common recognized issues with the classical programming
  // languages and their standard or common libraries is that these might be
  // hard to extend. If there is a bug or lack of feature in a standard library,
  // one have to wait for a next release for it to be fixed, and often these
  // never get fixed for years.
  //
  // Let's implement some functions we will use to explain the concept later.

  object EvolutionUtils0 {

    // Exercise 1:
    // Implement a `pow` method which calculates a power of number.
    //
    // I.e. `pow(4, 2) == 1` and `pow(3, 3) == 27`.
    def pow(base: Int, exponent: Int): Int = ???

    // Exercise 2:
    // Implement a concat method which concatenates two positive `Int`
    // numbers into one.
    //
    // I.e. `concat(72, 456) == 72456`.
    def concat(a: Int, b: Int): Int = ???

    // Exercise 3:
    // Implement a `toInstant` method which tries to parse a String
    // to a standard JVM instant representation.
    def toInstant(string: String): Option[Instant] = ???

    // Exercise 4:
    // Implement a `mean` method which calculates an average number.
    def mean(list: List[Int]): Int = ???

    // What is a common thing among these methods?
    // Where would you place them in your application if implemented?
  }

  // If you are C developer, you would, probably add prefixes to the methods
  // like this to not pollute a namespace:
  object EvolutionUtils1 {
    def int_pow(base: Int, exponent: Int): Int            = ???
    def int_concat(a: Int, b: Int): Int                   = ???
    def string_toInstant(string: String): Option[Instant] = ???
    def list_int_mean(list: List[Int]): Int               = ???
  }

  // Then we can call the methods like this:
  {
    EvolutionUtils1.int_pow(4, 2)
    EvolutionUtils1.int_concat(72, 456)
    EvolutionUtils1.string_toInstant("2012-10-01")
    EvolutionUtils1.list_int_mean(List(1, 2, 3, 4, 5))
  }

  // Or use imports and call them like this instead:
  {
    import EvolutionUtils1._

    int_pow(4, 2)
    int_concat(72, 456)
    string_toInstant("2012-10-01")
    list_int_mean(List(1, 2, 3, 4, 5))

    // Do you find this way convenient / readable?
  }

  // One issue is that these prefixes make the code hard to read and look messy.
  // We might go futher and, as well, use objects instead of prefixes:
  object EvolutionUtils2 {
    object IntUtils {
      def pow(base: Int, exponent: Int): Int = ???
      def concat(a: Int, b: Int): Int        = ???
    }
    object StringUtils {
      def toInstant(string: String): Option[Instant] = ???
    }
    object ListIntUtils {
      def mean(list: List[Int]): Int = ???
    }
  }

  // Then we can call more beautiful methods or even import them in parts of our
  // code where we use them more often.
  {
    import EvolutionUtils2._

    IntUtils.pow(4, 2)
    IntUtils.concat(72, 456)
    ListIntUtils.mean(List(1, 2, 3, 4, 5))

    import StringUtils._

    toInstant("2012-10-01")
  }

  // Do you find this way convenient / readable? What else we could do?

  // Another issue with this way to write things is that our methods does not
  // look natural / readable enough.
  //
  // I.e. we, probably want `4.pow(2)` and `72.concat(456)` instead of what we
  // have now.
  //
  // We cannot edit `Int` or `String` implementation to add more methods, so
  // we have to use some tricks instead.
  //
  // One way to fix it would be to introduce our own wrappers instead of using
  // objects.
  //
  object EvolutionUtils3 {

    case class RichInt(a: Int) {
      def pow(exponent: Int): Int = ???
      def concat(b: Int): Int     = ???
    }
    case class RichString(a: String) {
      def toInstant: Option[Instant] = ???
    }
    case class RichListInt(list: List[Int]) {
      def mean: Int = ???
    }

  }

  {
    import EvolutionUtils3._

    RichInt(4).pow(2)
    RichInt(72).concat(456)
    RichListInt((List(1, 2, 3, 4, 5))).mean

    // Do you find this way more convenient? More readable?

    // Exercise 5:
    // Implement the methods above so the tests pass.

  }

  // Do you see any issues with this approach?
  //
  // One issue is that calling a simple `pow` method on `Int` forces Scala to
  // create `RichInt` wrapper, causing memory allocation. If you do this in loop
  // it could be a relatively slow operation.
  //
  // For that purpose there is a special `AnyVal` class, which will make the
  // wrapper class very efficient. To save the time, we will not go into the
  // usage of `AnyVal`, though you are welcome to read about it here:
  // https://docs.scala-lang.org/overviews/core/value-classes.html
  //
  // If you are into Scala 3, you might want to read about opaque types instead:
  // https://docs.scala-lang.org/scala3/reference/other-new-features/opaques.html
  //
  // Another issue with this approach is that it is still quite ugly, and might
  // be hard to discover in IDE because we opted to include the argument type
  // into the name of our method.
  //
  // Here, finally, extension methods (or implicit classes), come in. We just add
  // `implicit` to our wrapper classes and we can now call the implemented methods
  // directly (!) on the types.
  //
  // I.e. we can do `4.pow(2)` instead of `RichInt(4).pow(2)`.
  //
  object EvolutionUtils4 {

    implicit class RichInt(a: Int) {
      def pow(exponent: Int): Int = ???
      def concat(b: Int): Int     = ???
    }
    implicit class RichString(a: String) {
      def toInstant: Option[Instant] = ???
    }
    implicit class RichListInt(list: List[Int]) {
      def mean: Int = ???
    }

  }
  {
    import EvolutionUtils4._

    // Exercise 5:
    // Use the new method directly on type without using a wrapper:
    RichInt(4).pow(2)
    RichInt(72).concat(456)
    RichListInt((List(1, 2, 3, 4, 5))).mean

    // Do you find this way more convenient? More readable?

  }

}

// How does this actually work? We will discuss this in the last section of this
// lecture. For now we can assume for all means and purposes that it just adds
// the wrapper class around the call.
//
// Note: your IDE can help you to identify where the method comes from.
// Metals: "Toggle showing implicit conversions and classes".
// IntelliJ: "Ctrl + Alt + Shift + "+" (I have no IntelliJ, check it yourself)
//
// See also: https://docs.scala-lang.org/overviews/core/implicit-classes.html
//
// We will discuss how Scala compiler finds the implicit classes (or does
// implicit resolution) later today if we have enough time.

// *Summary*
//
// Extension methods using implicit classes is a powerful technique allowing
// one to add the useful methods to existing libraries and improving the
// readability of the code.
//
// It should be noted though, that by adding the methods to _existing_ code,
// you are changing the language and increasing the knowledge that needs to
// be absorbed by the the incoming developer.
//
// That why it should be used sparsely. I.e. if you can avoid extending the
// classes this way, please make a favour to your colleagues and do avoid it.
// Make it as simple as possible: just add a new method to class if class
// is possible to edit.
//
// There are the reasons when extending the classes should be preferred and
// also the ways to minimize the discoverability issues arising from the new
// added methods. This discussion is out of scope of this lecture and will
// be touched more heavily during a typeclass lecture instead.

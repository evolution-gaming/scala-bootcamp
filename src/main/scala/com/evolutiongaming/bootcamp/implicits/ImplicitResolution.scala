package com.evolutiongaming.bootcamp.implicits

// You might find yourself surprised at this point of time. We, supposedly,
// learned implicits, but still did not feel why these are so, probably
// unfairly, infamous.
//
// The cool part is that you can build a very advanced patterns out of the
// simple building blocks we learned. Sometimes even too advanced and cryptic.
//
// Let's learn some syntax sugar and advanced technques now.
//
object ImplicitResolution extends App {

  // *Implicit resoltion*
  //
  // You might find yourself surprised at this point of time. We, supposedly,
  // learned implicits, but still did not feel why these are so, probably
  // unfairly, infamous.
  //
  // The cool part is that you can build a very advanced patterns out of the
  // simple building blocks we learned. Sometimes even too advanced and cryptic.
  //
  // Let's learn some syntax sugar and advanced technques now.
  //

  // We will use the following set of classes to explain the concepts:
  trait Printer {
    def print(string: String): Unit
  }
  object Printer {
    def default: Printer = new Printer {
      def print(string: String) = println(string)
    }
    def evil: Printer    = new Printer {
      def print(string: String) = sys.error(string)
    }
    def silent: Printer  = new Printer {
      def print(string: String) = ()
    }
  }

  def destroyTheWorld(): Unit = ()

  def congratulate(name: String)(implicit printer: Printer): Unit =
    printer.print(s"Congratulation! You just destroyed the world, $name!")

  def disappoint()(implicit printer: Printer): Unit =
    printer.print("Everything is fine :(")

  // Methods we will work with:
  def completelySafeMethodToCall(name: String)(implicit printer: Printer): Unit = {
    destroyTheWorld()
    congratulate(name)
  }
  def doNotCallThisIsUnsafe()(implicit printer: Printer): Unit                  = {
    disappoint()
  }

  // First let's start with some syntax sugar. We know that we can pass
  // implicit parameter by specifying it explicitly like this:
  completelySafeMethodToCall("John")(Printer.default)
  doNotCallThisIsUnsafe()(Printer.default)

  // It is not always convenient, because you might not want to pass it to
  // every top level method you want to call with implicit or because you
  // want implicits to be defined globally in some predefined `object`:

  {
    implicit val printer = Printer.default
    completelySafeMethodToCall("John")
    doNotCallThisIsUnsafe()
  }

  // Do you like the new approach? Does it look more verbose or less verbose?
  // Would you use it in your application?

  // In our example, we defined implicit value locally. Let's see how else we
  // could do it:

  // define implicit value globally
  object MyImplicits {
    implicit val printer = Printer.default
  }
  {
    import MyImplicits.printer
    completelySafeMethodToCall("John")
    doNotCallThisIsUnsafe()
  }

  // define implicit value globally with wildcard import
  {
    import MyImplicits._
    completelySafeMethodToCall("John")
    doNotCallThisIsUnsafe()
  }

  // define imports in companion object of required type :O
  //
  // (uncomment and add implicit to `Printer` companion object)
  // {
  //   completelySafeMethodToCall("John")
  //   doNotCallThisIsUnsafe()
  // }

  // How do we find out which one worked out? Where does Scala compiler (!)
  // find the implicit parameters?
  //
  // One way is to use IDE of course. Another is to remember these resolution
  // rules:
  //
  // 1. First local, inherited and package scope is searched for.
  // 2. Then explicit imports (`import Something.implicit` ) are taken into account.
  // 3. Wildcard imports (`import Something._`) are searched in.
  // 4. Package implicits are read the last.

  // Exercise 1: Find out if companion object implicits have a precendence
  // over local implicits by using different printers and running them.
  {
    // println("== Exercise started ==")
    // completelySafeMethodToCall("John")
    // doNotCallThisIsUnsafe()
    // println("== Exercise finished ==")
  }

}

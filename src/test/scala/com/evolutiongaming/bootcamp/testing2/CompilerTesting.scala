package com.evolutiongaming.bootcamp.testing2

import org.scalatest.funsuite.AnyFunSuite

// *Introduction*
//
// The bug which is not respresentable using the code will never happen.
// The simplest example of such defense is introduction of type system. I.e.
// if you have marked your field as `Integer` in your Java app, we do not need
// to test if it contains `String` inside. It simply cannot.
//
// Scala is much more powerful in that area, i.e., for example, we can make
// compile check if number is positive, if string is an actual e-mail etc. which
// is called refined types (https://github.com/fthomas/refined). We can also
// make sure the part of the code never accesses the database unless asked to do
// so etc., which is called effect tracking (https://typelevel.org/cats-effect/).
//
// It is not just a cool rocket science tech, we are using this stuff every day,
// and most Scala developers here won't be surprised if you ask them about it.
object PowerfulScala {

  // Excercise 1
  //
  // Prove Scala is at least as powerful as Java. Make sure that we cannot
  // call `energy("wrong stuff")`.
  //
  // Run the suite using the command below:
  //
  // sbt:scala-bootcamp> testOnly *testing2.Excersise2Spec
  //
  def energy(mass: String): String = {
    val speedOfLight = BigDecimal(299792458)
    val energy = BigDecimal(mass) * speedOfLight.pow(2)
    energy.toString
  }

}
class PowerfulScalaSpec extends AnyFunSuite {

  test("we get a correct result") {
    assert(PowerfulScala.energy("100") == "8987551787368176400")
  }
  test("wrong call does not compile") {
    assertTypeError("""PowerfulScala.energy("wrong stuff")""")
  }

}
object Parametricity {

  // Excercise 2
  //
  // You, probably, heard about "parametric reasoning" previously during these
  // lectures. Let's repeat the material a bit again.
  //
  // There is a famous paper by Philop Walder called "Theorems for free!"
  //
  // Quote:
  // > Write down the definition of a polymorphic function on a piece of paper.
  // > Tell me its type, but be careful not to let me see the function’s
  // > definition. I will tell you a theorem that the function satisfies.
  //
  // Let's do some excersise to understand the concept (shamelessly stolen
  // from the article of Daniel Sebban)

  // Implement the following function in all possible ways:
  def f1_way1[A](a: A): A = ???
  def f1_way2[A](a: A): A = ???

  // Let's do another one...
  def f2_way1[A](a: A, b: A): A = ???
  def f2_way2[A](a: A, b: A): A = ???
  def f2_way3[A](a: A, b: A): A = ???

  // Can this function use `a` somehow in implementation?
  def f3[A](a: A, b: Int): Int = ???

  // How about this one?
  def f4[A](a: A, b: Int): Int = ???

  // Implement the following function in several ways:
  // What is common in all of these implementations?
  def f5_way1[A](as: List[A]): List[A] = ???
  def f5_way2[A](as: List[A]): List[A] = ???
  def f5_way3[A](as: List[A]): List[A] = ???

  // How many ways we can implement this function with?
  def f6[A, B](as: List[A]): List[B] = ???

  // How about this one?
  def f7[A](a: A): Int = ???

}

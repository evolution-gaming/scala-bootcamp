package com.evolutiongaming.bootcamp.tf

/*
  == Materials I used:

  The real deal — papers by Oleg Kiselyov (Haskell, 18+)
  http://okmij.org/ftp/tagless-final/index.html
  https://okmij.org/ftp/tagless-final/course/lecture.pdf
  see also
  https://okmij.org/ftp/tagless-final/course/Boehm-Berarducci.html

  Rock the JVM, Tagless Final in Scala
  https://www.youtube.com/watch?v=m3Qh-MmWpbM
  https://blog.rockthejvm.com/tagless-final/

  == Links from previous speakers on the topic

  Что такое tagless final? (Scala 3, история развития кодировок от Черча до TF)
  https://www.youtube.com/watch?v=ZNK57IXgr3M

  Tagless Final lessons series
  https://www.youtube.com/watch?v=XJ2NjqkWdck&list=PLJGDHERh23x-3_T3Dua6Fwp4KlG0J25DI

  Practical FP in Scala
  https://leanpub.com/pfp-scala (Chapters 3, 4)
 */

import cats._
import cats.implicits._
import cats.effect._

object Numbers extends IOApp {
  // =============== Initial

  sealed trait Expression

  final case class Const(x: Int)                            extends Expression
  final case class Negate(x: Expression)                    extends Expression
  final case class Add(left: Expression, right: Expression) extends Expression
  // XXX: Uncomment this line to *break* evaluate and show functions.
  // final case class Multiply(left: Expression, right: Expression) extends Expression

  // 8 + -3 + 4
  // 8 add neg(3) add 4
  // 8 add neg(3) add 4
  // add(8, add(neg(3), 4))
  //
  // infix: one + another
  // prefix: + one another
  val expression = Add(Const(8), Add(Negate(Const(3)), Const(4)))

  // _ <- IO.delay(println(("initial:", expression, evaluate(expression), show(expression))))

  // Interpretation #1 of ADT/expression
  def evaluate(expression: Expression): Int = expression match {
    case Const(n)         => n
    case Negate(n)        => -evaluate(n)
    case Add(left, right) => evaluate(left) + evaluate(right)
    // case Multiply(left, right) => evaluate(left) * evaluate(right)
  }

  // Interpretation #2 of ADT/expression
  def show(expression: Expression): String = expression match {
    case Const(n)         => s"$n"
    case Negate(n)        => s"-(${show(n)})"
    case Add(left, right) => s"(${show(left)}) + (${show(right)})"
    // case Multiply(left, right) => s"${show(left)} * ${show(right)}"
  }

  // =============== Final

  trait ExpressionArith[A] {
    def const(x: Int): A
    def negate(a: A): A
    def add(left: A, right: A): A
  }

  // 8 + (-3) + 4
  def expressionA[A](interpreter: ExpressionArith[A]): A = {
    import interpreter._

    add(const(8), add(negate(const(3)), const(4)))
  }

  val intInterpreter = new ExpressionArith[Int] {
    override def const(x: Int): Int              = x
    override def negate(x: Int): Int             = -x
    override def add(left: Int, right: Int): Int = left + right
  }

  val stringInterpreter = new ExpressionArith[String] {
    override def const(x: Int): String                    = s"$x"
    override def negate(x: String): String                = s"-($x)"
    override def add(left: String, right: String): String = s"($left) + ($right)"
  }

  // XXX: Comment this line out to show that it doesn't break anything.
  trait ExpressionMult[A] {
    def multiply(left: A, right: A): A
  }

  val intMultInterpreter = new ExpressionMult[Int] {
    override def multiply(left: Int, right: Int): Int = left * right
  }

  val stringMultInterpreter = new ExpressionMult[String] {
    override def multiply(left: String, right: String): String = s"$left * $right"
  }

  // 8 + (-3) * 4
  def expressionAM[A](interpreterA: ExpressionArith[A], interpreterM: ExpressionMult[A]): A = {
    import interpreterA._
    import interpreterM._

    add(const(8), multiply(negate(const(3)), const(4)))
  }

  override def run(args: List[String]): IO[ExitCode] =
    for {
      _ <- IO.delay(println(("initial:", expression, evaluate(expression), show(expression))))
      _ <- IO.delay(println(("final:", expressionA(intInterpreter), expressionA(stringInterpreter))))
      _ <- IO.delay(
        println(
          (
            "final mult:",
            expressionAM(intInterpreter, intMultInterpreter),
            expressionAM(stringInterpreter, stringMultInterpreter),
          )
        )
      )
    } yield ExitCode.Success
}

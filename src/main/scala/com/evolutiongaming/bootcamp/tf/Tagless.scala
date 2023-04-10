package com.evolutiongaming.bootcamp.tf

import cats._
import cats.implicits._
import cats.effect._

object Tagless extends IOApp {
  // =============== Typed Tagless Initial

  sealed trait Expression[A]

  final case class Bool(b: Boolean)                                          extends Expression[Boolean]
  final case class Or(left: Expression[Boolean], right: Expression[Boolean]) extends Expression[Boolean]
  // final case class And(left: Expression[Boolean], right: Expression[Boolean]) extends Expression[Boolean]

  final case class Integer(x: Int)                                    extends Expression[Int]
  final case class Negate(x: Expression[Int])                         extends Expression[Int]
  final case class Add(left: Expression[Int], right: Expression[Int]) extends Expression[Int]

  // 8 + -3 + 4
  val expressionI: Expression[Int]     =
    Add(Integer(8), Add(Negate(Integer(3)), Integer(4)))
  val expressionB: Expression[Boolean] = Or(Bool(false), Bool(true))
  // val expressionBad: Expression[Boolean] = Or(Integer(false), Bool(3))

  def evaluate[A](expression: Expression[A]): A = expression match {
    case Bool(b)          => b
    case Or(left, right)  => evaluate(left) || evaluate(right)
    case Integer(n)       => n
    case Negate(n)        => -evaluate(n)
    case Add(left, right) => evaluate(left) + evaluate(right)
  }

  def show[A](expression: Expression[A]): String = expression match {
    case Bool(b)          => s"$b"
    case Or(left, right)  => s"${evaluate(left)} || ${evaluate(right)}"
    case Integer(n)       => s"$n"
    case Negate(n)        => s"-(${show(n)})"
    case Add(left, right) => s"(${show(left)}) + (${show(right)})"
  }

  // =============== Typed Tagless Final

  trait ExpressionArith[F[_]] {
    def const(x: Int): F[Int]
    def negate(a: F[Int]): F[Int]
    def add(left: F[Int], right: F[Int]): F[Int]
  }

  // meaning: 8 + (-3) + 4
  // lecture note:
  //   show how cats implements F[_]: ExpressionBool without import
  //   mention difference between implicit and bound
  // def expressionA[F[_]](interpreter: ExpressionArith[F]): F[Int] = {
  def expressionA[F[_]: ExpressionArith]: F[Int] = {
    val interpreter = implicitly[ExpressionArith[F]]
    import interpreter._

    add(const(8), add(negate(const(3)), const(4)))
  }

  final case class Expr[A](a: A) extends AnyVal

  val intInterpreter = new ExpressionArith[Expr] {
    override def const(x: Int): Expr[Int]                          = Expr(x)
    override def negate(x: Expr[Int]): Expr[Int]                   = Expr(-x.a)
    override def add(left: Expr[Int], right: Expr[Int]): Expr[Int] =
      Expr(left.a + right.a)
  }

  final case class ExprShow[A](s: String) extends AnyVal

  val intShowInterpreter = new ExpressionArith[ExprShow] {
    override def const(x: Int): ExprShow[Int]                                  = ExprShow(s"$x")
    override def negate(x: ExprShow[Int]): ExprShow[Int]                       = ExprShow(s"-${x.s}")
    override def add(left: ExprShow[Int], right: ExprShow[Int]): ExprShow[Int] =
      ExprShow(s"${left.s} + ${right.s}")
  }

  trait ExpressionBool[F[_]] {
    def bool(x: Boolean): F[Boolean]
    def or(left: F[Boolean], right: F[Boolean]): F[Boolean]
  }

  // false || true
  def expressionB(interpreter: ExpressionBool[Expr]): Expr[Boolean] = {
    import interpreter._

    or(bool(false), bool(true))
  }

  val boolInterpreter = new ExpressionBool[Expr] {
    override def bool(x: Boolean): Expr[Boolean]                              = Expr(x)
    override def or(left: Expr[Boolean], right: Expr[Boolean]): Expr[Boolean] =
      Expr(
        left.a || right.a
      )
  }

  // lecture note:
  // you can make a langauge, if you use both ExpressionArith,
  // ExpressionBool and add expressions that combine them

  override def run(args: List[String]): IO[ExitCode] =
    for {
      _ <- IO.delay(
        println(("initial:", evaluate(expressionB), show(expressionB), evaluate(expressionI), show(expressionI)))
      )
      _ <- IO.delay(
        println(
          (
            "final arithmetic:",
            expressionA(intInterpreter),
            expressionA(intShowInterpreter),
          )
        )
      )
      _ <- IO.delay(
        println(
          (
            "final boolean:",
            expressionB(boolInterpreter),
          )
        )
      )
    } yield ExitCode.Success
}

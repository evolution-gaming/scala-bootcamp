package com.evolutiongaming.bootcamp.tf.v2

object InitialEncoding extends App {

  sealed trait Expression
  object Expression {
    final case class Const(x: Int)                            extends Expression
    final case class Negate(e: Expression)                    extends Expression
    final case class Add(left: Expression, right: Expression) extends Expression
  }

  import Expression._

  // 13 + (-(1 + 2))
  val expr = Add(
    Const(13),
    Negate(Add(Const(1), Const(2)))
  )

  def evaluate(expr: Expression): Int =
    expr match {
      case Const(x)         => x
      case Negate(e)        => -evaluate(e)
      case Add(left, right) => evaluate(left) + evaluate(right)
    }

  println(evaluate(expr))

  def show(expr: Expression): String =
    expr match {
      case Const(x)         => s"$x"
      case Negate(e)        => s"(-${show(e)})"
      case Add(left, right) => s"(${show(left)} + ${show(right)})"
    }

  println(show(expr))

  sealed trait Multiplication
  object Multiplication {
    final case class Multiply(left: Expression, right: Expression) extends Multiplication
  }

  def evaluate2(expr: Multiplication): Int =
    expr match {
      case Multiplication.Multiply(left, right) => evaluate(left) * evaluate(right)
    }

  import Multiplication._

  // (13 + (-(1 + 2))) * 2
//  val multiplyExpression =
//    Multiply(
//      Add(
//        Const(13),
//        Negate(Add(Const(1), Const(2)))
//      ),
//      Const(2)
//    )
//
//  val multiplyExpression2 =
//    Add(
//      Negate(Add(Const(1), Const(2))),
//      Multiply(Const(2), Const(2))
//    )
}

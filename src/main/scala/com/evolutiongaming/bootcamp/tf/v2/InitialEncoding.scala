package com.evolutiongaming.bootcamp.tf.v2

object InitialEncoding extends App {

  sealed trait Expression
  object Expression {
    final case class Const(x: Int)                            extends Expression
    final case class Negate(e: Expression)                    extends Expression
    final case class Add(left: Expression, right: Expression) extends Expression
  }
}

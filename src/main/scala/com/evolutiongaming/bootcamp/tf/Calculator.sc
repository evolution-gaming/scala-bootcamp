/*
  Additional materials:

  Papers by Oleg Kiselyov http://okmij.org/ftp/tagless-final/index.html
  (Haskell, 18+)

  Что такое tagless final? https://www.youtube.com/watch?v=ZNK57IXgr3M
  (Scala 3, история развития кодировок от Черча до TF)

  Tagless Final lessons series https://www.youtube.com/watch?v=XJ2NjqkWdck&list=PLJGDHERh23x-3_T3Dua6Fwp4KlG0J25DI

  Practical FP in Scala https://leanpub.com/pfp-scala (Chapters 3, 4)
*/

import cats._
import cats.syntax.all._


sealed trait Expression

final case class Const(x: Int) extends Expression
final case class Add(left: Expression, right: Expression) extends Expression
final case class Multiply(left: Expression, right: Expression) extends Expression

//  2 * 3 + 4
val expression = Add(
  Multiply(Const(2), Const(3)),
  Const(4)
)

def evaluate(expression: Expression): Int = expression match {
  case Const(n)              => n
  case Add(left, right)      => evaluate(left) + evaluate(right)
  case Multiply(left, right) => evaluate(left) * evaluate(right)
}

def show(expression: Expression): String = expression match {
  case Const(n)              => s"$n"
  case Add(left, right)      => s"(${show(left)} + ${show(right)})"
  case Multiply(left, right) => s"${show(left)} * ${show(right)}"
}





sealed trait DivideExpression
final case class Divide(left: Expression, right: Expression) extends DivideExpression

def evaluate2(expression: DivideExpression): Int = expression match {
  case Divide(left, right) => evaluate(left) / evaluate(right)
}


// 6 / 3 * 2
//val expression3 = Multiply(
//  Divide(Const(6), Const(3)),
//  Const(2)
//)



trait ExpressionA[A] {
  def const(x: Int): A
  def add(left: A, right: A): A
  def multiply(left: A, right: A): A
}

val intInterpreter = new ExpressionA[Int] {
  override def const(x: Int): Int = x
  override def add(left: Int, right: Int): Int = left + right
  override def multiply(left: Int, right: Int): Int = left * right
}

val freeInterpreter = new ExpressionA[Expression] {
  override def const(x: Int): Expression = Const(x)
  override def add(left: Expression, right: Expression): Expression = Add(left, right)
  override def multiply(left: Expression, right: Expression): Expression = Multiply(left, right)
}

//  2 * 3 + 4
def expression3[A](interpreter: ExpressionA[A]): A = {
  import interpreter._

  add(
    multiply(const(2), const(3)),
    const(4)
  )
}




trait DivideExpressionA[A] {
  def divide(left: A, right: A): A
}

val intDivideAlgebra = new DivideExpressionA[Int] {
  override def divide(left: Int, right: Int): Int = left / right
}

// 6 / 3 * 2
def expression4[A](interpreter: ExpressionA[A], divideInterpreter: DivideExpressionA[A]): A = {
  import interpreter._, divideInterpreter._

  multiply(
    divide(const(6), const(3)),
    const(2)
  )
}




trait ExpressionTF[F[_], A] {
  def const(x: A): F[A]
  def add(left: F[A], right: F[A]): F[A]
  def multiply(left: F[A], right: F[A]): F[A]
  def divide(left: F[A], right: F[A]): F[A]
}

object ExpressionTF {

  def intInterpreter[F[_]: Monad: MonoidK]: ExpressionTF[F, Int] = new ExpressionTF[F, Int] {
    override def const(x: Int): F[Int] = x.pure[F]
    override def add(left: F[Int], right: F[Int]): F[Int] = (left, right).mapN(_ + _)
    override def multiply(left: F[Int], right: F[Int]): F[Int] = (left, right).mapN(_ * _)
    override def divide(left: F[Int], right: F[Int]): F[Int] =
      (left, right).tupled.flatMap {
        case (x, y) if y == 0 => MonoidK[F].empty
        case (x, y)           => (x / y).pure[F]
      }
  }
}


val dsl = ExpressionTF.intInterpreter[List]
import dsl._

// (3, 2) + 3
add(
  const(3) ::: const(2),
  divide(const(6), const(2))
)

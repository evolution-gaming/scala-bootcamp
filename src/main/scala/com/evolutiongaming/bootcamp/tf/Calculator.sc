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

def show(expression: Expression): String = ???
















trait ExpressionTF[F[_], A] {
  def const(x: A): F[A]
  def add(left: F[A], right: F[A]): F[A]
  def multiply(left: F[A], right: F[A]): F[A]
  def divide(left: F[A], right: F[A]): F[A]
}

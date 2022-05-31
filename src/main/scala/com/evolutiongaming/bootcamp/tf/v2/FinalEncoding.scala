package com.evolutiongaming.bootcamp.tf.v2

import cats.{Applicative, Apply, Functor, Id, MonadError, Semigroupal}
import cats.syntax.all._

object FinalEncoding extends App {

  trait ExpressionA[F[_], A] {
    def const(n: Int): F[A]
    def negate(a: F[A]): F[A]
    def add(a1: F[A], a2: F[A]): F[A]
  }

  // 13 + (-(1 + 3))
  def program1[F[_], A](interpreter: ExpressionA[F, A]): F[A] = {
    import interpreter._
    add(
      const(13),
      negate(add(const(1), const(3)))
    )
  }

  def intInterpreter[F[_]: Applicative]: ExpressionA[F, Int] =
    new ExpressionA[F, Int] {
      override def const(n: Int): F[Int]               = n.pure[F]
      override def negate(a: F[Int]): F[Int]           = a.map(a => -a)
      override def add(a1: F[Int], a2: F[Int]): F[Int] = (a1, a2).mapN { _ + _ }
    }

  def stringInterpreter[F[_]: Applicative]: ExpressionA[F, String] =
    new ExpressionA[F, String] {
      override def const(n: Int): F[String]                     = s"$n".pure[F]
      override def negate(a: F[String]): F[String]              = a.map(a => s"(-$a)")
      override def add(a1: F[String], a2: F[String]): F[String] =
        (a1, a2).mapN((a1, a2) => s"($a1 + $a2)")
    }

  trait Multiplication[F[_], A] {
    def multiply(a1: F[A], a2: F[A]): F[A]
  }

  def intMultiplicationAlgebra[F[_]: Functor: Semigroupal]: Multiplication[F, Int] =
    new Multiplication[F, Int] {
      override def multiply(a1: F[Int], a2: F[Int]): F[Int] =
        (a1, a2).mapN((a1, a2) => a1 * a2)
    }

  def stringMultiplicationAlgebra[F[_]: Apply]: Multiplication[F, String] =
    new Multiplication[F, String] {
      override def multiply(a1: F[String], a2: F[String]): F[String] =
        (a1, a2).mapN { case (a1, a2) => s"($a1 * $a2)" }
    }

  // (13 * 2) + (-(1 + 3))
  def program2[F[_], A](expression: ExpressionA[F, A], multiplication: Multiplication[F, A]): F[A] = {
    import expression._
    import multiplication._
    add(
      multiply(const(13), const(2)),
      negate(add(const(1), const(3)))
    )
  }

  trait Division[F[_], A] {
    def divide(a1: F[A], a2: F[A]): F[A]
  }

  def intDivision[F[_]: MonadError[*[_], String]]: Division[F, Int] =
    new Division[F, Int] {
      override def divide(a1: F[Int], a2: F[Int]): F[Int] =
        (a1, a2).tupled.flatMap {
          case (_, 0)   => "Division by zero".raiseError[F, Int]
          case (a1, a2) =>
            if (a1 % a2 == 0) (a1 / a2).pure[F]
            else "Division ended up with a modulo".raiseError[F, Int]
        }
    }

  def stringDivision[F[_]: Apply]: Division[F, String] =
    new Division[F, String] {
      override def divide(a1: F[String], a2: F[String]): F[String] =
        (a1, a2).mapN { case (a1, a2) => s"($a1 * $a2)" }
    }

  // ((13 * 2) + (-(1 + 3))) / 2
  def program3[F[_], A](
    expression: ExpressionA[F, A],
    multiplication: Multiplication[F, A],
    division: Division[F, A]
  ): F[A] = {
    import expression._
    import multiplication._
    import division._
    divide(
      add(
        multiply(const(13), const(2)),
        negate(add(const(1), const(3)))
      ),
      const(0)
    )
  }

  // (13 * 2) + (-((1 + 3) / 2))
  def program4[F[_], A](
    expression: ExpressionA[F, A],
    multiplication: Multiplication[F, A],
    division: Division[F, A]
  ): F[A] = {
    import expression._
    import multiplication._
    import division._
    add(
      multiply(const(13), const(2)),
      negate(divide(add(const(1), const(3)), const(2)))
    )
  }

  println(program1[Id, String](stringInterpreter))
  println(program1[Id, Int](intInterpreter))

  println("*" * 100)

  println(program2[Option, String](stringInterpreter, stringMultiplicationAlgebra))
  println(program2[Id, Int](intInterpreter, intMultiplicationAlgebra))

  println("*" * 100)

  println(program3[Id, String](stringInterpreter, stringMultiplicationAlgebra, stringDivision))
  println(program3[Either[String, *], Int](intInterpreter, intMultiplicationAlgebra, intDivision))

  println("*" * 100)

  println(program4[Id, String](stringInterpreter, stringMultiplicationAlgebra, stringDivision))
  println(program4[Either[String, *], Int](intInterpreter, intMultiplicationAlgebra, intDivision))
}

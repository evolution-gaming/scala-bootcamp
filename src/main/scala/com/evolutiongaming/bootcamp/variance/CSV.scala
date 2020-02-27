package com.evolutiongaming.bootcamp.variance

import cats.{Applicative, ContravariantMonoidal}
import cats.implicits._

object CSV extends App {

  trait CSVEncoder[A] {
    def encode(value: A): List[String]
  }

  implicit final class EncoderSyntax[A: CSVEncoder](private val value: A) {
    def encode: List[String] = implicitly[CSVEncoder[A]].encode(value)
  }

  trait CSVDecoder[A] {
    def decode(csv: List[String]): (A, List[String])
  }

  implicit final class DecoderSyntax(private val csv: List[String]) {
    def decode[A: CSVDecoder]: A = implicitly[CSVDecoder[A]].decode(csv)._1
  }



  implicit def applicative[A]: Applicative[CSVDecoder] = new Applicative[CSVDecoder] {

    override def pure[A](value: A): CSVDecoder[A] = (csv: List[String]) => (value, csv)

    override def ap[A, B](ff: CSVDecoder[A => B])(fa: CSVDecoder[A]): CSVDecoder[B] = (csv: List[String]) => {
      val (f, restA) = ff.decode(csv)
      val (a, rest) = fa.decode(restA)
      (f(a), rest)
    }
  }

  implicit def divide[A]: ContravariantMonoidal[CSVEncoder] = new ContravariantMonoidal[CSVEncoder] {

    override def unit: CSVEncoder[Unit] = _ => Nil

    override def product[A, B](fa: CSVEncoder[A], fb: CSVEncoder[B]): CSVEncoder[(A, B)] =
      (value: (A, B)) => fa.encode(value._1) ::: fb.encode(value._2)

    override def contramap[A, B](fa: CSVEncoder[A])(f: B => A): CSVEncoder[B] = (value: B) => fa.encode(f(value))
  }



  implicit val stringEncoder: CSVEncoder[String] = (value: String) => List(value)
  implicit val intEncoder:    CSVEncoder[Int]    = stringEncoder.contramap(_.toString)

  implicit val stringDecoder: CSVDecoder[String] = (csv: List[String]) => (csv.head, csv.tail)
  implicit val intDecoder:    CSVDecoder[Int]    = stringDecoder.map(_.toInt)



  final case class Person(name: String, age: Int)
  object Person {
    implicit val encoder: CSVEncoder[Person] = (stringEncoder, intEncoder).contramapN(person => (person.name, person.age))
    implicit val decoder: CSVDecoder[Person] = (stringDecoder, intDecoder).mapN(Person.apply)
  }



  println(Person("John", 42).encode)
  println(List("John", "42").decode[Person])



  // TODO: implement CSV encpding/decoding
  final case class Account(person: Person, active: Boolean, amount: BigDecimal)
}

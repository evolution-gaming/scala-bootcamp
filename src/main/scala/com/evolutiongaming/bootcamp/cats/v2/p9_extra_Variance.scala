package com.evolutiongaming.bootcamp.cats.v2

import java.time.Instant

object p9_extra_Variance {
  /*
       Covariant | Contravariant | Invariant
       useful reading https://www.oreilly.com/library/view/learning-scala/9781449368814/ch10.html Type Variance
   */
  trait Fruit
  trait Apple extends Fruit
  case object Orange extends Fruit
  case object Banana extends Fruit

  case object Golden extends Apple
  case object GrannySmith extends Apple

  object Covariance {

    /**
      * Covariance is a type of relationship between two parametrized types where
      * if A is subtype of B, then (for example) F[A] is a subtype of F[B]
      * scala's Seq is defined as a covariant type.
      * In the following example List[Orange], List[Banana] and List[Apple] are subtypes of List[Fruit]
      * thus we can concatenate them and have an instance of List[Fruit]
      */
    val apples: List[Apple] = List(Golden, GrannySmith)
    val oranges: List[Orange.type] = List(Orange)
    val bananas: List[Banana.type] = List(Banana)

    val fruits: List[Fruit] = apples ++ oranges ++ bananas
  }

  object Contravariance {

    /**
      * Contravariance is a type of relationship between two parametrized types where
      * if A is subtype of B, then (for example) F[B] is a subtype of F[A]
      * Think of variance as of directions of morphing.
      */
    sealed trait Enc[-A] { // try to change it to +A
      def encode(a: A): String
    }

    val fruitEnc: Enc[Fruit] = new Enc[Fruit] {
      override def encode(a: Fruit): String = a.toString
    }

    val appleEnc: Enc[Apple] = fruitEnc
    val orangeEnc: Enc[Orange.type] = fruitEnc
    val bananaEnc: Enc[Banana.type] = fruitEnc

    fruitEnc.encode(GrannySmith)

    /** Useful reading https://stackoverflow.com/questions/38034077/what-is-a-contravariant-functor
      * Contravariant functor has a `contramap` function defined
      * For example we have types A and B, function `f` from B to A and
      * an instance of F[B] and we need to get F[A]
      * Ex e.1 Complete the definition of Printer
      * */
    trait Printer[-A] {
      self =>
      def print(a: A): String

      def contramap[B](f: B => A): Printer[B] = new Printer[B] {
        override def print(a: B): String = ???
      }
    }

    val bigDecPrinter: Printer[BigDecimal] = _.toString
    val longPrinter: Printer[Long] = bigDecPrinter.contramap(BigDecimal.apply)
    val instantSecPrinter: Printer[Instant] = longPrinter.contramap(_.getEpochSecond)

    trait Bird
    trait Goose extends Bird

    val birdPrinter: Printer[Bird] = _.toString
    val goosePrinter: Printer[Goose] = birdPrinter
  }

  object Invariance {

    /**
      * No relationship whatsoever. Doesnt matter how A and B relate to each other,
      * F[A] is never subtype of F[B] and vice versa.
      */


    trait InvariantPrinter[A] {
      def print(a: A): String
    }

    trait Animal
    trait Dog extends Animal

    val animalPrinter: InvariantPrinter[Animal] = _.toString

    // the following line won't compile.
    //val dogPrinter: InvariantPrinter[Dog] = animalPrinter

  }
}

package com.evolutiongaming.bootcamp.basics

object Mutability {
  import scala.collection.mutable

  // scala has both mutable and immutable collections.
  // immutable versions imported by default and you need to explicitly specify you want mutable version.

  // val vs var, mutable immutable
  // something which is obvious for java programmers but may be different in some other languages

  // immutable map
  val m1: Map[Int, String] = Map(1 -> "a", 2 -> "b")
  m1 + (3 -> "c") // returns a new collection

  // map is immutable but you can assign a new map to m2
  var m2: Map[Int, String] = Map(1 -> "a", 2 -> "b")
  m2 = Map(1 -> "a")

  // map is mutable but you cannot assign a new map to m3
  val m3: mutable.Map[Int, String] = mutable.Map(1 -> "a")
  m3 += (2 -> "b")

  // can both be reassigned and changed
  var m4: mutable.Map[Int, String] = mutable.Map(1 -> "a")
  m4 += (2 -> "b")
  m4 = mutable.Map(1 -> "a")
}

object MutabilityAndRt extends {
  // in functional programming it is ok to use a mutable collection if it never leaks from a function

  def mkString[T](l: List[T]): String = {
    val a = new StringBuilder() // mutable thing
    l.foreach(x => a.append(x)) // mutation
    a.result()
  }
  // from the outside function is pure so you can do this but avoid if you can
}

// (!mutable!) is a Scala's representation for Java's T[]
object JavaArray extends App {
  val a = Array(1, 2, 3)
  a(2) = 4 // can be mutated, not super cool
  println(a.toList)
}

// immutable array alternative
object CoolArray extends App {
  val a = Vector(1, 2, 3) // this cool array is in fact a tree
  // a(2) = 4  --  will not compile
  val b = a.updated(2, 4) // cool immutable collection with performant random reads and updates
  println(b)
}

object Sets extends App {
  val beautifulNumber = Set(3, 5, 7, 123) // they are not special in any way, just beautiful to me at this particular moment

  // the same as contains
  println(beautifulNumber(3))
  println(beautifulNumber(4))
}

object Maps extends App {
  val map = Map(1 -> "a", 2 -> "b")
  val map2 = Map((1, "a"), (2, "b")) // same map
  println(map(2)) // gets element and throws exception
  println(map.get(3)) // gets element safely

  println(map.withDefaultValue("lalala")(3)) // uses default
  println(map.withDefaultValue("lalala").get(3)) // shows only real elements
}

object LinkedList extends App {

  /*
    Linked lists work great for functional programming:
      - it is efficient in memory
      - it is quick to add to the beginning
      - cool pattern matching
   */

  // list is recursive
  object MyList {

    sealed trait List[+T]

    object Empty extends List[Nothing]

    case class NonEmpty[T](head: T, tail: List[T]) extends List[T]

    NonEmpty(1, NonEmpty(2, NonEmpty(3, Empty)))
  }

  val list = List(1, 2, 3, 4, 5, 6)
  val list2 = List("a", "b", "c", "d", "e")

  // collections nice methods
  list.head
  list.tail
  list.init
  list.last

  list.headOption
  list.lastOption
  list.minOption
  list.maxOption

  list.sum
  list.product
  list.reverse
  list.distinct
  list.indices

  list.sorted // implicit ordering
  list.sortBy(-_)
  list.sortWith { (a, b) => a > b }

  // there will be a separate lecture on implicits but lets have a look now
  object ImplicitParams {
    case class User(id: Int)
    val l: List[User] = List(User(1), User(3), User(2))
    l.sortBy(_.id)
    l.sortWith { (u1, u2) => u1.id < u2.id }

    implicit val ord: Ordering[User] = Ordering.by(_.id) // sorted won't compile if we don't provide an Ordering for it
    l.sorted
  }

  // they are the same
  list.drop(2).take(2)
  list.slice(2, 4)

  list.take(2).intersect(list.take(5))

  list.contains(2)
  list.exists(_ % 2 == 0)
  list.count(_ % 2 == 0)
  list.find(_ % 2 == 0)
  list.filter(_ % 2 == 0)
  list.filterNot(_ % 2 == 0)
  val (trues, elses) = list.partition(_ % 2 == 0) // returns two lists
  list.forall(_ % 2 == 0) // returns true on empty

  list.groupBy(_ / 5)

  list.map(_ * 2)
  list.flatMap(x => List(x, x))
  List(list, list, list).flatten

  // Zips
  list.zipWithIndex // == list.zip(list.indices)
  list zip list.tail
  list zip list2

  /*
     In a sorted list find two numbers which have a gap between
        None for List(1, 2, 3, 4)
        Some((2, 8)) for List(1, 2, 8)
   */
  def findGap(l: List[Int]): Option[(Int, Int)] = {
    ???
  }

  // recursion
  def sum(list: List[Int]): Int = list match {
    case h :: tail => h + sum(tail)
    case Nil => 0
  }

  // folds
  list.foldLeft(0) { (acc, x) => acc + x }
  list.foldRight(0) { (acc, x) => acc + x }
  list.fold(0) { (acc, x) => acc + x }
  list.reduce(_ + _)
  list.reduceLeft(_ + _)
  list.reduceRight(_ + _)

  // try to implement min different ways (fold, reduce, recursion)
  def min(list: List[Int]): Option[Int] = {
    ???
  }

  // Implement scanLeft (not using scans ofc)
  def scanLeft[T](zero: T)(list: List[T])(f: (T, T) => T): List[T] = {
    ???
  }

  // https://twitter.com/allenholub/status/1357115515672555520/photo/1
  // pass the interview
  def count(s: String): List[(Char, Int)] = {
    ???
  }

  /*
    Additional information:

    In some other functional languages foldRight (Haskell) is lazy and works on Infinite collections.
    Haskell foldRight can be found in scalaz/cats.
    See: http://voidmainargs.blogspot.com/2011/08/folding-stream-with-scala.html
   */

  object WhyTwoParamLists {
    def map1[A, B](l: List[A], f: A => B): List[B] = l map f
//    map1(List(1, 2, 3), x => x + 1) // compiler cannot understand what type is x
    map1(List(1, 2, 3), (x: Int) => x + 1) // so you have to tell the type

    def map2[A, B](l: List[A])(f: A => B): List[B] = l map f
    map2(List(1, 2, 3))(x => x + 1) // and now compiler is smarter
    map2(List(1, 2, 3))(_ + 1) // so you can even do this
  }

  object TheFourDots { // ! additional information which can be skipped !
    // a method on list which ends in : so it is associates to right
    1 :: 2 :: 3 :: Nil

    // a two param case class
    list match {
      case Nil =>
      case head :: tail =>
    }

    list match {
      case Nil =>
      case ::(head, tail) =>
    }

    case class MultipliedBy(a: Int, b: Int)

    MultipliedBy(5, 6) match {
      case a MultipliedBy b => a * b
    }

    // all this is very fun but nobody really does this (RIP scalaz \/)
    val map1: String Map Int = Map("a" -> 1)
    val map2: Map[String, Int] = Map("a" -> 1)

    // but you don't need to care just use :: and have fun
  }
}

object PassingManyParams extends App {

  // Syntax for many params
  def method[T](ts: T*): Unit = println(ts.head)

  // correct usage
  method(1, 2, 3)

  // passes seq of lists
  val list = List(1, 2, 3)
  method(list)

  // correct usage
  method(list: _*)
}

// Lets get back to [+T]
object Variance {
  /*
      +------------------------+------------------------+
      |       MyType[+T]       |       MyType[-T]       |
      +------------------------+------------------------+
      | A -> B                 | A -> B                 |
      | MyType[A] -> MyType[B] | MyType[A] <- MyType[B] |
      +------------------------+------------------------+

    Lets say Int is a Number

    List has + because
    if I want List[Number] I can have List[Int] instead

    JsonPrinter (it converts a type to json) would have - because
    if I want JsonPrinter[Int] I can have JsonPrinter[Number] as it can print my Int as well as any other Number
   */

  /*
    Function is -A => +B
    if i need a function which returns Numbers I am ok with functions returning Int since Int is a Number
    if i need a function which processes Ints I am ok with functions processing any Number
   */

  type Color

  class Animal
  class Cat extends Animal

  val describe: Animal => Color = ???
  val describeCat: Cat => Color = describe // if you can describe any animals then you can describe cats as well

  val createCat: Color => Cat = ???
  val create: Color => Animal = createCat // if you want to create a red animal then creating red cat fits your needs
}

// hometask:
// https://leetcode.com/problems/running-sum-of-1d-array/
// https://leetcode.com/problems/shuffle-the-array
// https://leetcode.com/problems/richest-customer-wealth
// https://leetcode.com/problems/kids-with-the-greatest-number-of-candies/
// https://leetcode.com/problems/widest-vertical-area-between-two-points-containing-no-points

// optional hometask:
//
// https://leetcode.com/problems/maximum-nesting-depth-of-the-parentheses/
// https://leetcode.com/problems/split-a-string-in-balanced-strings
// https://leetcode.com/problems/matrix-block-sum/

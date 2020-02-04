package com.evolutiongaming.bootcamp.functions

import java.time.Instant
import java.util.Date

import scala.util.Try

object Functions {
  // Functions is first-class values:
  // a functions can be assigned to a value, passed as a parameter and returned as a result

  // first order functions acts with simple data types
  // higher order functions that takes/returns functions

  // Example.
  def clean(message: String): String = message.replaceAll("fox", "***")

  def mkUpperCase(message: String): String = message.toUpperCase

  // Pass our logic as a parameter `f`
  def processText(message: String, f: String => String): String = f(message)

  def clean2(message: String): String = {
    // `s` is a parameter and may be omitted
    val f: String => String = s => s.replaceAll("fox", "***")
    processText(message, f)
  }

  def mkUpperCase2(message: String): String = {
    val f: String => String = _.toUpperCase
    processText(message, f)
  }

  // Exercise.
  // Implement `isEven` a function that checks if a number is even
  def isEven(x: Int): Boolean = x % 2 == 0

  // Implement `isEvenVal` which behaves exactly like `isEven`.
   val isEvenVal: Int => Boolean = _ % 2 == 0

  // Implement `isEvenDefToVal` by transforming `isEven` def function into a val
   val isEvenDefToVal: Int => Boolean = isEven

  // --


  // In Scala, every concrete type is a type of some class or trait
  // `(String => String)` is the same as scala.Function1[String, String]
  // `scala.Function1[A, B]` is a trait, where `A` and `B` are type parameters

  // an instance of a function can be treated as object

  // The simplified version of the scala.Function1
  trait MyFunction1[T, R] {
    // `apply` defines how we transform `T` to `R`
    def apply(v1: T): R
  }

  // More common way to define a function type is just `A => B`
  // `A => B` is the type of a function that takes an arg of type A and return a result of type B

  // Syntax sugar allows to call a function w/o typing `apply`
  // `f.apply(..)` becomes `f(..)`

  // We can write a function w/o giving a name
  processText("some text", _ + "!!")

  // Anonymous function expands to implementation of scala.Function1 trait
  processText("some text", new Function1[String, String] {
    override def apply(v1: String): String = v1 + "!!"
  })

  // Method can be passed as a function, but it is not a function value, it's just converted automatically
  def trimAndWrap(v: String): String = s"<${v.trim}>"

  processText("xxx", trimAndWrap)


  // Subclassing Functions
  // One nice aspect of functions being traits is that we can subclass the function type

  trait MyMap[K, V] extends (K => V)

  // Question. What should we extend to ..

  // check if an element belongs to a set
  // > trait MySet[A] extends ???

  // return a value by its index
  // > trait MySeq[A] extends ???

  // --

  // Polymorphic functions has at least one type parameter
  // A type parameter is a form of encapsulation

  def x[T](v: T) = ???

  // Exercise.
  // Implement `mapOption` a function. Do not use scala option api
  def mapOption[A, B](option: Option[A], f: A => B): Option[B] = option match{
    case None => None
    case Some(v)=> Some(f(v))
  }

  // Implement `identity` which returns its input unchanged. Do not use scala.Predef.identity
  def identity[A](x: A): A = x

  // --


  // The pattern matching block expands to the Function1 instance
  val pingPong: String => String = {
    case "ping" => "pong"
  }

  // Question. What happens next?
  // > pingPong("hi?")

  // With the function type itself we cannot find out beforehand
  // whether the function is applicable to a certain argument

  // Partial functions is another trait which extends Function and has `isDefinedAt` method

  val pingPongPF: PartialFunction[String, String] = {
    case "ping" => "pong"
  }

  pingPongPF.isDefinedAt("ping") // > true
  pingPongPF.isDefinedAt("hi") // > false


  // If expected type is a PF then a pattern matching block will expended to PF implementation

  val pingPongPFImpl: PartialFunction[String, String] = new PartialFunction[String, String] {
    override def isDefinedAt(x: String): Boolean = x match {
      case "ping" => true
      case _ => false
    }

    override def apply(v: String): String = v match {
      case "ping" => "pong"
    }
  }

  // Question. What do you expect?

  val f1: PartialFunction[List[String], Boolean] = {
    // head :: tail
    case _ :: _ => true
  }

  // 1
  val result1: Boolean = f1.isDefinedAt(List("false", "true"))

  val f2: PartialFunction[List[String], Boolean] = {
    case Nil => false
    // head :: 2nd :: tail
    case _ :: _ :: tail => f1(tail)
  }

  // 2
  val result2: Boolean = f2.isDefinedAt(List("false", "true"))

  // --


  // We can make a function that returns another function
  // Example.
  type Language = String

  def translate(message: String, from: Language, to: Language): String = {
    // some logic
    if (from == to) message else message.reverse
  }

  def translateFromRus: (Language, String) => String =
    (to: String, message: Language) => translate(message, "rus", to)

  // `=>` has right associative law
  def translateF: Language => Language => String => String =
    (from: Language) => (to: Language) => (message: String) => translate(message, from, to)

  val fromRu = translateF("ru")
  val fromRuToEn = fromRu("en")
  val result = fromRuToEn("функция")

  // Multiple parameter lists ~ syntax sugar for functions returning a function
  def translateM(from: Language)(to: Language)(message: String): String = translate(message, from, to)

  // --


  // Functions can be used as building blocks of our program using the composition of functions
  // `scala.Function1[A, B]` has `compose` and `andThen` methods that takes a function param and returns a new function

  // Compose - `g` will be applied to input param
  // def compose[A](g: A => T1): A => R = { x => apply(g(x)) }

  val double: Int => Int = (x: Int) => 2 * x
  val addString: Int => Language = (a: Int) => "new value " + a

  addString.compose(double)

  // AndThen - `g` will be applied to output result
  // def andThen[A](g: R => A): T1 => A = { x => g(apply(x)) }

  double.andThen(addString)

  List(1, 2, 3).map(_ + 2).map(_.toString)
  List(1, 2, 3).map(((x: Int) => x + 2).andThen(x => x.toString))


  // Exercise. Implement `andThen` and `compose` which pipes the result of one function to the input of another function
  def compose[A, B, C](f: B => C, g: A => B): A => C = a => f(g(a))

  def andThen[A, B, C](f: A => B, g: B => C): A => C = a => g(f(a))


  // --



  // Pure functions are mappings between two sets

  // A function is impure if ..
  // - is not defined for all values of input type
  // - throws an exception
  // - returns a value that depends on something else than an input value
  // - works with mutable shared state
  // - does something that is not present in the function signature (side effects)
  // - relies on reflection

  // Why is Null bad?
  // null causes NullPointerException
  // null cannot be removed from language
  // w/o any compiler check null can be passed anywhere

  // Exercise. Provide an example of an impure functions

  // Is `plus` a pure function? why?
  def plus(a: Int, b: Int): Int = a + b

  // Is `mapLookup` a pure function? why?
  def mapLookup(map: Map[String, Int], key: String): Int =
    map.apply(key)

  // Pure function should:
  // - be total (not partial): its return value is the same for the same arguments
  // - not throw exception
  // - be deterministic
  // - not do any mutation (local, non-local, reference, etc.)
  // - not have side effect
  // - not use null

  // A function without side effects only returns a value


  // Exercise. Provide an example of pure functions
  // Question. If a function return for all inputs the same value, is this function pure?

  // Benefits of pure functions

  // Fearless refactoring: any value can be replaced by the function that produced it (referential transparency)
  // Documentations based on functions types
  // Easier to test: no mutation, no randomness, no side effect
  // Potential compiler optimisations
  // Make parallel processing easier


  // Exercises. Convert the following function into a pure function.
//  type Try = Try // just to make it compile and indicate that return type should be changed

  //
  def parseDate(s: String): Instant = Instant.parse(s)
  def parseDatePure(s: String): Try[Instant] = Try(Instant.parse(s))

  //
  def divide(a: Int, b: Int): Int = a / b
//  def dividePure(a: Int, b: Int) = b match {
//    case b == 0 => None
//    case _ => Some(a / b)
//  }

  //
  var count = 0
  def id(): Int = {
    val newId = count
    count += 1
    newId
  }
  def idPure(id: Int): (Int, Int) = (id, id + 1)

  //
  def isAfterNow(date: Instant): Boolean = date.isAfter(Instant.now())
  def isAfterNowPure(date: Instant, now: Instant): Boolean = date.isAfter(now)

  //
  case class Nel[T](head: T, rest: List[T])
  def nel[T](list: List[T]): Nel[T] = {
    if (list.isEmpty) println("ERROR: provide non empty list")
    Nel(list.head, list.tail)
  }
  def nelPure[T](list: List[T]): Option[Nel[T]] = list match {
    case Nil => None
    case h :: t => Some(Nel(h, t))
  }

  // --



  // Final task.
  // Case classes are Scala's preferred way to define complex data

  val rawJson: String =
    """
      |{
      |   "username":"John",
      |   "address":{
      |      "country":"UK",
      |      "postalCode":45765
      |   },
      |   "eBooks":[
      |      "Scala",
      |      "Dotty"
      |   ]
      |}
  """.stripMargin

  // Representing JSON in Scala as a sealed family of case classes
  // JSON is a recursive data structure
  sealed trait Json

  case class JObject(jsonMap: Map[String, Json]) extends Json {
    override def toString: String = s"""{${jsonMap.map({ case (key, value) => s""""$key":$value"""}).mkString(",")}}"""
  }

  case class JArray(value: Array[Json]) extends Json {
    override def toString: String = s"[${value.mkString(",")}]"
  }

  case class JString(value: String) extends Json {
    override def toString: String = s""""$value""""
  }

  case class JNumber(value: BigDecimal) extends Json {
    override def toString: String = s"""$value"""
  }

  case class JBoolean(value: Boolean) extends Json

  // Question. What did I miss?

 case class JDate(value: Date) extends Json



  // Task 1. Represent `rawJson` string via defined classes
  val data: Json = JObject(Map(
    "username" -> JString("John"),
    "address" -> JObject(Map(
      "country" -> JString("UK"),
      "postalCode" -> JNumber(45765)
    )),
    "eBooks" -> JArray(Array(JString("Scala"), JString("Dotty")))
  ))

  // Task 2. Implement a function `asString` to print given Json data as a json string

  def asString(data: Json): String = data.toString

  // Task 3. Implement a function that validate our data whether it contains JNumber with negative value or not

  def isContainsNegative(data: Json): Boolean = data match {
    case JNumber(value) => value < 0
    case JObject(valueMap) => valueMap.exists { case (_, value) => isContainsNegative(value) }
    case JArray(valueArray) => valueArray.exists(isContainsNegative)
    case _ => false
  }

  // Task 4. Implement a function that return the nesting level of json objects.
  // Note. top level json has level 1, we can go from top level to bottom only via objects

  def nestingLevel(data: Json): Int = data match {
    case JObject(valueMap) => 1 + valueMap.map{ case(_, value) => nestingLevel(value) }.max
    case _ => 0
  }

  // See FunctionsSpec for expected results


  // Additional
  // https://www.scala-exercises.org/std_lib/higher_order_functions
  // https://www.scala-exercises.org/fp_in_scala/getting_started_with_functional_programming
}
import scala.annotation.unchecked.uncheckedVariance

sealed trait Animal extends Product with Serializable

case class Cat() extends Animal {
  def meow: String = "meow"
}
case class Dog() extends Animal {
  def woof: String = "woof"
}

def secondElementOfIntList[A](xs: List[A]): Option[A] = xs match {
  case _ :: x :: _ => Some(x)
  case _           => None
}

secondElementOfIntList(List(1, 4, 3))
secondElementOfIntList(List(1))

case class Triple[+A](first: A, second: A, third: A) {
  def toList: List[A]                       = List(first, second, third)
  def withFirst[A1 >: A](a: A1): Triple[A1] = Triple(a, second, third)
  def map[B](f: A => B): Triple[B]          = ???
}

val t = Triple[String]("a", "b", "c")
t.toList

// def changeTriple(t: Triple[Animal]) = {
//     t.first = Cat()
// }

val tripleDogs = Triple(Dog(), Dog(), Dog())
tripleDogs.withFirst(Cat())
// changeTriple(tripleDogs: Triple[Animal])
val dogs       = List(Dog(), Dog(), Dog())

val dogsAndCat = Cat() :: dogs
// tripleDogs
// tripleDogs.first.woof

// def foo[A, B][C](x: Int)(s: Int, y: Int)() = () incorrect
// def foo(x: Int)[A, B] = () incorrect

type Matrix[+A] = Vector[Vector[A]]

val m: Matrix[Int] = Vector(Vector(1, 2), Vector(3, 4))

val u: Matrix[Any] = m :+ Vector("aaa", "bbb")

// least upper bound / LUBg
Map("cat" -> Cat()) ++ Map("dog" -> Dog())

type Printers[-A] = List[A => String]

val printers: Printers[Animal] = List(
  {
    case _: Cat => "I'm a cat"
    case _: Dog => "I'm a dog"
  },
  x => x.toString,
)

printers.map(_.apply(Dog()))
printers.map(_.apply(Cat()))

val dogPrinters = printers :+ ((dog: Dog) => dog.woof)
dogPrinters.map(_.apply(Dog()))

val animalPrinters: Printers[Animal] = dogPrinters.asInstanceOf[Printers[Animal]]
// animalPrinters.map(_.apply(Cat()))

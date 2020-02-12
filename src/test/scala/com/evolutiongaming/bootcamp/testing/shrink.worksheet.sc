import org.scalacheck.Shrink

// trait Shrink[T] {
//   def shrink(x: T): Stream[T]
// }

Shrink.shrink(42)

Shrink.shrink("Hello world!")

Shrink.shrink((0 to 5).toList)


sealed trait Pet
object Pet {
  case object Cat extends Pet
  case class Dog(breed: String) extends Pet
}

implicit val petShrink = Shrink[Pet] {
  case Pet.Cat => Shrink.shrink(Pet.Cat)
  case Pet.Dog(breed) => Shrink.shrink(breed).map(Pet.Dog) :+ Pet.Cat
}

Shrink.shrink[Pet](Pet.Dog("shepherd"))


// Implement shrink for recursive ADT.

sealed trait Tree
object Tree {
  case object Leaf extends Tree
  case class Node(l: Tree, r: Tree) extends Tree
}

implicit val treeShrink: Shrink[Tree] = ???

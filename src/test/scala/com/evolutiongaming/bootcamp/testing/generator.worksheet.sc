import org.scalacheck.Gen

// trait Gen[T] {
//   def apply(p: Gen.Parameters, seed: Seed): Option[T]
// }

val numberGen = Gen.choose(-10, 10)
numberGen.sample

val mapGen = numberGen.map(_ * 2)
mapGen.sample

val tupleGen = for {
  n <- numberGen
  m <- Gen.choose(n, 10)
} yield (n, m)
tupleGen.sample

val filterGen = numberGen.filter(_ > 0)
filterGen.sample

val letterGen = Gen.oneOf('A', 'E', 'I', 'O', 'U')
letterGen.sample

val biasedLetterGen = Gen.frequency(
  (5, 'A'),
  (4, 'E'),
  (3, 'I'),
  (2, 'O'),
  (1, 'U'),
)

val listGen = Gen.listOf(letterGen)
listGen.sample

val biasedListGen = Gen.listOfN(20, biasedLetterGen)
biasedListGen.sample

val stringGen = Gen.alphaNumStr
stringGen.sample


// Implement a generator for list of length from 5 to 10.

val listOfNMGen: Gen[List[Int]] = ???
listOfNMGen.sample


// Implement a generator for ADT.

sealed trait Pet
object Pet {
  case object Cat extends Pet
  case class Dog(breed: String) extends Pet
}

val petGen: Gen[Pet] = ???
petGen.sample


// Implement a generator for recursive ADT.

sealed trait Tree
object Tree {
  case object Leaf extends Tree
  case class Node(l: Tree, r: Tree) extends Tree
}

def treeGen(depth: Int): Gen[Tree] = ???
treeGen(3).sample

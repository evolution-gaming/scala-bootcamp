import org.scalacheck.{Arbitrary, Gen}

// trait Arbitrary[T] {
//   def arbitrary: Gen[T]
// }

val numberGen = Arbitrary.arbitrary[Int]
numberGen.sample

val boolGen = Arbitrary.arbitrary[Boolean]
boolGen.sample

val stringGen = Arbitrary.arbitrary[String]
stringGen.sample

val listGen = Arbitrary.arbitrary[List[Int]]
listGen.sample


sealed trait Pet
object Pet {
  case object Cat extends Pet
  case class Dog(breed: String) extends Pet
}

val petGen: Gen[Pet] = ???

implicit val petArb = Arbitrary(petGen)

Arbitrary.arbitrary[Pet].sample

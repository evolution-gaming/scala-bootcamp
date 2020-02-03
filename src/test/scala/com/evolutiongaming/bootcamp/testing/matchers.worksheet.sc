import org.scalatest.matchers.should.Matchers._

// http://doc.scalatest.org/3.1.0/org/scalatest/matchers/should/Matchers.html

// Checking equality with matchers

val number = 3

number should equal (3) // can customize equality
number should === (3)   // can customize equality and enforce type constraints
number should be (3)    // cannot customize equality, so fastest to compile
number shouldEqual 3    // can customize equality, no parentheses required
number shouldBe 3       // cannot customize equality, so fastest to compile, no parentheses required

// Checking size, length, emptiness

val list = List(1, 2, 3)

list should have length 3
list should have size 3

list should not be empty

// Checking strings

val string = "Hello world"

string should startWith ("Hello")
string should endWith ("world")
string should include ("ll")

string should startWith regex "Hel*o"
string should endWith regex "wo.ld"
string should include regex "o.ld"

string should fullyMatch regex "H.*d"

// Greater and less than

number should be < 7
number should be > 0
number should be <= 7
number should be >= 0

// Checking an object's class

list shouldBe a [List[_]]
list should not be a [Set[_]]
list should not be an [Int]

// Checking numbers against a range

number should equal (4 +- 1)
number should === (4 +- 1)
number should be (4 +- 1)
number shouldEqual 4 +- 1
number shouldBe 4 +- 1

// Working with containers

list should contain (2)

list should contain oneOf (0, 3, 9)
list should contain oneElementOf Set(0, 3, 9)

list should contain noneOf (7, 8, 9)
list should contain noElementsOf Set(7, 8, 9)

list should contain atLeastOneOf (1, 3, 9)
list should contain atLeastOneElementOf Set(0, 3, 9)

list should contain atMostOneOf (0, 3, 9)
list should contain atMostOneElementOf Set(0, 3, 9)

list should contain allOf (1, 2)
list should contain allElementsOf Set(1, 2)

(list ++ list) should contain only (1, 2, 3)

list should contain theSameElementsAs List(3, 2, 1)

(list ++ list).sorted should contain inOrderOnly (1, 2, 3)

(0 :: list ++ list).sorted should contain inOrder (1, 2, 3)
(0 :: list ++ list).sorted should contain inOrderElementsOf Set(1, 2, 3)

list shouldBe sorted

val map = Map(1 -> 2)

map should contain key 1
map should contain value 2

// Checking that a snippet of code does not compile

"val a: Double = ,1" shouldNot compile

"val a: String = 1" shouldNot typeCheck

"val a: Int = 1" should compile

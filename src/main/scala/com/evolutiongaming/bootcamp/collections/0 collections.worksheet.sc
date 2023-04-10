import scala.collection.immutable.SortedMap
import scala.collection.immutable.NumericRange
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.ArrayDeque
import scala.collection.immutable.SortedSet
import scala.collection.immutable.Queue
import scala.collection.mutable.PriorityQueue
import scala.collection.immutable.BitSet
import scala.collection.compat.immutable.ArraySeq
import scala.collection.concurrent.TrieMap
// format: off
/**
 * ╔═══════════════════════════════════════════════╗
 * ║                                               ║ 
 * ║   W e l c o m e    t o   E v o l u t i o n    ║
 * ║    M e e t u p / O p e n    L e c t u r e     ║
 * ║                                               ║
 * ╚═══════════════════════════════════════════════╝
 */


// format: on

type A = List[Int] //Strict
type B = Set[Int] // Strct
type C = TrieMap[Int, Int]
type D = Map[Int, Int] //Strict
type E = ArraySeq[Int]
type F = BitSet
type G = Vector[Int]
type H = PriorityQueue[Int]
type I = Queue[Int]
type J = SortedSet[Int]
type K = ArrayDeque[Int]
type L = Array[Int]
type M = LazyList[Int]
type N = Range
// type O = Stream[Int]
type P = String
type Q = ListBuffer[Int]
type R = NumericRange[Long]
type S = SortedMap[Int, Int]

// Persistent Data Structure

def foo(x: => Int) = 1

foo(throw new RuntimeException("oops"))

// Map(1 -> (throw new RuntimeException("oops")))

val it = List(1, 2, 3).iterator.map(_ + 1).map(_ + 1)
it.toList

var counter = 0
val lview   = List(1, 2, 3).view.map(_ + 1).map { x =>
  counter += 1
  x + 1
}

lview.toList
lview.toList

counter

val fibonacci: LazyList[BigInt] =
  LazyList[BigInt](1, 1) #::: fibonacci.lazyZip(fibonacci.tail).map(_ + _)

fibonacci.take(1000).force

val list = List(Left(1), Right("a"), Left(2), Right("b"))
list.collect { case Right(x) => x }

fibonacci.take(10).foldLeft(BigInt(0))(_ + _)

fibonacci.take(10).scanLeft(BigInt(0))(_ + _)

list.collectFirst { case Right(x) => x }

fibonacci.take(10).groupBy(_ % 2)

val pairs = List((1, "one"), (2, "two"), (1, "uno"), (2, "duo"), (3, "three"))
pairs.groupBy(_._1)
pairs.groupMap(_._1)(_._2)
pairs.groupMapReduce(_._1)(_._2)((x, y) => s"$x, $y")

// val x: Option[Int] = None

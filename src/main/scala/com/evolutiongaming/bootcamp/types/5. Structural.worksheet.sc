import scala.collection.immutable.VectorImpl
import scala.languageFeature.reflectiveCalls
//format: off

/**
  * ╔════════════════════════════════════════╗
  * ║    S t r u c t u r a l    t y p e s    ║
  * ╚════════════════════════════════════════╝
  */

//format: on

type A_B = {
  def a: Int
  def b(x: Int): String
}

object SimpleObject {
  val a         = 1
  def b(x: Int) = s"squirrel $x"
}

val simpleObject: A_B = SimpleObject

simpleObject.a
simpleObject.b(2)

type A_C = {
  def a: Int
  def c[A]: A
}

type A_B_C = A_B with A_C

def sub[A, B >: A] = ()

def eq[A, B >: A <: A] = {
  sub[A, B]
  sub[B, A]
}

// eq[A_B, A_C]

eq[
  A_B_C, {
    def a: Int
    def b(x: Int): String
    def c[A]: A
  },
]

sub[
  {
    val a: Int
  }, {
    def a: Int
  },
]

sub[
  {
    def calc(x: Int): String
  }, {
    def calc(x: Int): Any
  },
]

// sub[
//   {
//     def calc(x: Int): String
//   }, {
//     def calc(x: Int): String
//   }
// ]

type HaveSomeType = {
  type SomeType
}

sub[
  {
    type SomeType <: Int
    def calc: SomeType
  }, {
    def calc: Int
  },
]

sub[
  {
    type SomeType >: List[String] <: Seq[Any]
    def calc: SomeType
  }, {
    type SomeType
    def calc: SomeType
  },
]


//format: off
/**
 * ┌──────────────────┐
 * │    Refinements   │   
 * └──────────────────┘
 */
// format: on

trait Transformer[-A, +B] {
  self =>
  type State

  def init: State

  def transformOne(a: A, state: State): State

  def result(state: State): B
}

class SumTransformer extends Transformer[Long, Long] {
  type State = Long

  def init: State = Numeric[Long].zero

  def transformOne(a: Long, state: State): State = a + state

  def result(state: State): Long = state
}

val transformer: Transformer[Long, Long] { type State = Long } = new SumTransformer

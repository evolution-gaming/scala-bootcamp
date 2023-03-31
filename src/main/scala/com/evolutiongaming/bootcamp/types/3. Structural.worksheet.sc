import scala.collection.immutable.VectorImpl
// import scala.languageFeature.reflectiveCalls
//format: off

/**
  * ╔════════════════════════════════════════╗
  * ║    S t r u c t u r a l    t y p e s    ║
  * ╚════════════════════════════════════════╝
  */

//format: on

type A_B = {
  def a: Int
  def b: String
}

object SimpleObject {
  val a = 1
  val b = "squirrel"
}

val simpleObject: A_B = SimpleObject

simpleObject.a
simpleObject.b

type A_C = {
  def a: Int
  def c: Double
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
    def b: String
    def c: Double
  }
]

sub[
  {
    val a: Int
  }, {
    def a: Int
  }
]

sub[
  {
    def calc(x: Int): Int
  }, {
    def calc(x: Any): Int
  }
]

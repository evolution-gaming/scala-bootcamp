
// format: off

/**
 * ╔═════════════════════════╗
 * ║  Abstract Type Members  ║
 * ╚═════════════════════════╝
 */

// format: on

trait InfoGetter {
  type Info
  def getInfo(key: Long): Info
  def printInfo(info: Info): String
}

def getInfos(keys: Long*)(getter: InfoGetter): Vector[String] = {
  keys.view.map(k => getter.printInfo(getter.getInfo(k))).toVector
}

getInfos(1, 2, 3)(new InfoGetter {

  override type Info = Long
  override def getInfo(key: Long): Long = key * 2
  override def printInfo(info: Long): String = info.toString
})


//format: off


















//format: on

/** ┌─────────────┐ │ Composition │ └─────────────┘
  */

trait Transformer[-A, +B] {
  self =>
  type State

  def init: State

  def transformOne(a: A, state: State): State

  def result(state: State): B

  def map[C](f: B => C): Transformer[A, C] = new Transformer[A, C] {
    type State = self.State

    def init = self.init

    def transformOne(a: A, state: State) = self.transformOne(a, state)

    def result(state: State): C = f(self.result(state))
  }

  def zip[A1 <: A, C](that: Transformer[A1, C]): Transformer[A1, (B, C)] =
    new Transformer[A1, (B, C)] {
      type State = (self.State, that.State)

      def init = (self.init, that.init)

      def transformOne(a: A1, state: State) = {
        val (selfState, thatState) = state
        (self.transformOne(a, selfState), that.transformOne(a, thatState))
      }

      def result(state: State): (B, C) = {
        val (selfState, thatState) = state
        (self.result(selfState), that.result(thatState))
      }
    }
}

def transform[A, B](
    as: Vector[A]
)(transformer: Transformer[A, B]): Vector[B] =
  as.scanLeft(transformer.init) { (state, a) =>
    transformer.transformOne(a, state)
  }.tail
    .map(transformer.result)

def result[A, B](as: Vector[A])(transformer: Transformer[A, B]): B =
  transformer.result(
    as.foldLeft(transformer.init) { (state, a) =>
      transformer.transformOne(a, state)
    }
  )

import Numeric.Implicits._

class SumTransformer[A: Numeric] extends Transformer[A, A] {
  type State = A

  def init: State = Numeric[A].zero

  def transformOne(a: A, state: State): State = a + state

  def result(state: State): A = state
}

transform(Vector(1, 2, 3, -3, -2, -1))(new SumTransformer[Int])

object QuantityTransformer extends Transformer[Any, Long] {
  type State = Long

  def init: State = 0

  def transformOne(a: Any, state: State): State = state + 1

  def result(state: State): Long = state
}

transform(Vector(1, 2, 3, -3, -2, -1))(QuantityTransformer)

def averageTransformer[A: Numeric]: Transformer[A, Double] =
  new SumTransformer[A].zip(QuantityTransformer).map { case (sum, quantity) =>
    sum.toDouble / quantity
  }

transform(Vector(1, 2, 3, -3, -2, -1))(averageTransformer[Int])


//format: off
/**
 * ┌────────┐
 * │ Mix-in │   
 * └────────┘
 */


trait Caching {
    type Key[A]

    private var cache = Map.empty[Key[_], Any]

    def cached[A](key: Key[A])(f: Key[A] => A): A = 
        cache.get(key) match {
            case Some(a) => a.asInstanceOf[A]
            case None => 
                val a = f(key)
                cache += key -> a
                a
        }
}

object Fibonacci extends Caching {
    sealed trait Key[A]
    case class Fib(n: Int) extends Key[BigInt]


    def fib(n: Int): BigInt = cached(Fib(n)) { case Fib(n) =>
        if (n <= 1) n
        else fib(n - 1) + fib(n - 2)
    }

}

Fibonacci.fib(100)


trait GeneralizedCaching {
    type Key[A]
    type Cache <: PartialFunction[Key[_], Any] 

    def init: Cache

    private var cache = init
    def add[A]( cache: Cache, key: Key[A], a: A): Cache

    def cached[A](key: Key[A])(f: Key[A] => A): A = 
        cache.applyOrElse(key, { _: Key[A] => 
                val a = f(key)
                cache = add(cache, key, a)
                a
        }).asInstanceOf[A]
}


import scala.collection.immutable.LongMap

trait LongCache extends GeneralizedCaching{

    type Key[A] <: Long
    type Cache = LongMap[Any]

    def init = LongMap.empty[Any]

    def add[A](cache: Cache, key: Key[A], a: A): Cache = cache + (key -> a)
}















// format: on

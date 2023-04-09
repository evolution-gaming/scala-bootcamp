import scala.collection.immutable.SortedMap
import scala.collection.immutable.HashMap
import scala.collection.immutable.MapOps
import scala.collection.MapFactory
import scala.util.Random
import com.evolutiongaming.bootcamp.collections.trie.TrieHashMap
import com.evolutiongaming.bootcamp.collections.trie.TrieVector
import com.evolutiongaming.bootcamp.collections.trie.IntTrie

// val x = TrieVector(1, 2, 3, 4, 5, 6, 7, 8, 9)

// val y = 9 +: 8 +: 7 +: 6 +: 5 +: 4 +: 3 +: 2 +: x

// y.map(_ + 100).map(_ % 10).take(10)

val m = TrieHashMap(1 -> "one", 2 -> "two", 3 -> "three")

m.updated(1, "uno")

def words() = {
  val rnd = new Random(87654321)

  Iterator.fill(10000)(Array.fill(2)(rnd.nextPrintableChar()).mkString)
}

def collectWords[M[k, +v] <: MapOps[k, v, M, M[k, v]]](factory: {
  def empty[K, V]: M[K, V]
}): M[String, Int] =
  words()
    .foldLeft(factory.empty[String, Int]) { case (map, word) =>
      map.updated(word, map.getOrElse(word, 0) + 1)
    }

// LazyList.from(1).reverse

collectWords(TrieHashMap) == collectWords(HashMap)

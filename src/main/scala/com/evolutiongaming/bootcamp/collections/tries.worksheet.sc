import com.evolutiongaming.bootcamp.collections.trie.TrieHashMap
import com.evolutiongaming.bootcamp.collections.trie.TrieVector
import com.evolutiongaming.bootcamp.collections.trie.IntTrie

val x = TrieVector(1, 2, 3, 4, 5, 6, 7, 8, 9)

val y = 9 +: 8 +: 7 +: 6 +: 5 +: 4 +: 3 +: 2 +: x

y.map(_ + 100).map(_ % 10).take(10)


val m = TrieHashMap(1 -> "one", 2 -> "two", 3 -> "three")

m.updated(1, "uno")


import com.evolutiongaming.bootcamp.collections.TrieVector
import com.evolutiongaming.bootcamp.collections.IntTrie

val x = TrieVector(1, 2, 3, 4, 5, 6, 7, 8, 9)

val y = 9 +: 8 +: 7 +: 6 +: 5 +: 4 +: 3 +: 2 +: x

y.map(_ + 100).map( _ % 10).take(10)

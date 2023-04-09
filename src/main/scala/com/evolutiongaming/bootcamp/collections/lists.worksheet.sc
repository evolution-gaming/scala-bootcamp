import scala.collection.immutable.HashMap
import com.evolutiongaming.bootcamp.collections.MyList.Cons
import com.evolutiongaming.bootcamp.collections.CherryTree
import com.evolutiongaming.bootcamp.collections.MyList

def uniqueRefs(ms: MyList[MyList[Any]]): Int =
  ms.iterator.flatMap(_.tails).map(System.identityHashCode).distinct.size - 1

val xs = MyList(1, 2, 3)

List(1, 2, 3, 4).to(MyList)

val xss = MyList(xs, MyList.Cons(4, xs))

def subsets[A](xs: MyList[A]) =
  0 to xs.length to MyList flatMap xs.combinations

subsets(MyList(1, 2, 3))

uniqueRefs(subsets(MyList(1, 2, 3)))
uniqueRefs(subsets(1 to 10 to MyList))

def subsets1[A](xs: MyList[A]): MyList[MyList[A]] = xs match {
  case Cons(head, tail) =>
    for (t <- subsets1(tail); l <- Array(t, head :: t)) yield l
  case MyList.Nil       => MyList(MyList.Nil)
}

subsets1(MyList(1, 2, 3))

uniqueRefs(subsets1(MyList(1, 2, 3)))

uniqueRefs(subsets1(1 to 10 to MyList))

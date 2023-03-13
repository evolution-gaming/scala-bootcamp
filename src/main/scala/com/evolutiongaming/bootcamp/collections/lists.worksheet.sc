import com.evolutiongaming.bootcamp.collections.MyList

val results = for {
  x <- MyList(1, 2, 3, 4, 5)
  y <- MyList.range(0, x)
} yield if (y == 0) Left(x) else Right((x, y))


results.collect{ case Right(x -> y) => x -> y }.groupMapReduce(_._2)(_._1)(_ + _)
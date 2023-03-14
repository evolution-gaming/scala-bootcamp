val fibs: fs2.Stream[fs2.Pure, BigInt] = 
    (fs2.Stream(BigInt(1), BigInt(2)) ++ fibs.zipWith(fibs.drop(1))(_ + _))


// println(fibl.take(100).toVector)
// println(fibs.take(100).compile.toVector)
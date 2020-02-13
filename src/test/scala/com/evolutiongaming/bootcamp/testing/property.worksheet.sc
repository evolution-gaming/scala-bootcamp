import org.scalacheck.Prop.{forAll, propBoolean}

val sortedSizeProp = forAll { l: List[Int] =>
  l.sorted.size == l.size
}

val sqrtProp = forAll { n: Int =>
  math.sqrt(n * n) == n
}

val evenProp = forAll { n: Int =>
  (n % 2 == 0) ==> (n % 2 == 0)
}

val oddProp = forAll { n: Int =>
  (n % 2 != 0) ==> (n % 2 != 0)
}

val intProp = evenProp && oddProp

val zeroProp = forAll { n: Int =>
  (n == 0) ==> (n + 1 == 1)
}

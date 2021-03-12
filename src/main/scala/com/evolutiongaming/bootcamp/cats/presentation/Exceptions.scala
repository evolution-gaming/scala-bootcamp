package com.evolutiongaming.bootcamp.cats.presentation

object Exceptions {

  type Error = String

  // safe divide
  implicit class IntExt(x: Int) {
    def divide(y: Int): Either[Error, Int] = {
      if (y == 0) Left("ERR") else Right(x / y)
    }
  }

  { // bad exceptions
    val x = 1 / 2
    val y = x / 3
    val z = y / 4
    val r = z / 5
  }

  {
    1 divide 2 match { // good referential transparency
      case Left(x) => Left(x)
      case Right(x) => x divide 3 match {
        case Left(x) => Left(x)
        case Right(x) => x divide 4 match {
          case Left(x) => Left(x)
          case Right(x) => x divide 5
        }
      }
    }
  }


  type Error2 = String
  // looks like imperative BUT has pure error handling
  for {
    x <- 1 divide 2
    y <- x divide 3
    z <- y divide 4
    r <- z divide 5
  } yield r

}

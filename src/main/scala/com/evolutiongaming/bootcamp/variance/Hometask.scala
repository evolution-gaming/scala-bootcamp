package com.evolutiongaming.bootcamp.variance

import java.awt.image.BufferedImage
import java.io.File

import cats.Comonad
import cats.implicits._
import javax.imageio.ImageIO

object Hometask extends App {

  sealed abstract case class Focus private (x: Int, y: Int)

  object Focus {

    val Zero: Focus = new Focus(0, 0) {}

    def of(x: Int, y: Int): Option[Focus] = (Option.when(x >= 0)(x), Option.when(y >= 0)(y)).mapN(new Focus(_, _) {})
  }

  sealed abstract case class Matrix[+A] private (matrix: Vector[Vector[A]], focus: Focus = Focus.Zero) {
    def get(x: Int, y: Int): Option[A] = matrix.get(x).flatMap(_.get(y))
  }

  object Matrix {

    def of[A](data: Iterable[A], rows: Int): Either[String, Matrix[A]] = for {
      _ <- Either.cond(rows > 0, (), "Rows count should be positive")
      _ <- Either.cond(data.size % rows == 0, (), "Every row must contain the same number of elements")
    } yield new Matrix[A](data.sliding(data.size / rows, data.size / rows).map(_.toVector).toVector) {}

    // Exercise: implement (you can construct Matrix directly here, ignoring smart constructor)
    implicit val comonad: Comonad[Matrix] = ???
  }




  type RGB   = (Int, Int, Int)
  type Image = Matrix[RGB]

  private def toRGB(rgb: Int): (Int, Int, Int) = {
    val red   = (rgb >> 16) & 0x000000FF
    val green = (rgb >> 8) & 0x000000FF
    val blue  = rgb & 0x000000FF

    (red, green, blue)
  }

  private def readImage(file: String): Image = {
    val image = ImageIO.read(getClass.getResourceAsStream(file))
    val data  = for {
      x <- 0 until image.getHeight
      y <- 0 until image.getWidth
    } yield toRGB(image.getRGB(x, y))
    Matrix.of(data, image.getHeight).getOrElse(sys.error("Failed to read image"))
  }

  private def writeImage(data: Image, file: String): Unit = {
    val image = new BufferedImage(data.matrix(0).size, data.matrix.size, BufferedImage.TYPE_INT_RGB)
    for {
      x   <- data.matrix.indices
      y   <- data.matrix(0).indices
      rgb =  data.matrix(x)(y)
    } image.setRGB(x, y, rgb._1 << 16 | rgb._2 << 8 | rgb._3)
    ImageIO.write(image, "bmp", new File("src/main/resources", file))
  }

  private def smooth(radius: Int)(image: Image): RGB = {
    val points = (-radius / 2 until radius / 2).toList
    val sum = (points, points).mapN { case (x, y) =>
      image.get(image.focus.x + x, image.focus.y + y).getOrElse((0xFF, 0xFF, 0xFF))
    }.combineAll
    val sqr = radius * radius
    (sum._1 / sqr, sum._2 / sqr, sum._3 / sqr)
  }

  private def mirror(image: Image): RGB = {
    val mirror = (image.matrix(0).size - 1) - image.focus.x
    image.matrix(mirror)(image.focus.y)
  }

  val kitty    = readImage("/cat.jpg")
  val smoothed = kitty.coflatMap(smooth(8))
  val mirrored = kitty.coflatMap(mirror)

//  writeImage(smoothed, "smoothed.bmp")
//  writeImage(mirrored, "mirrored.bmp")

  require(smoothed == readImage("/smoothed.bmp"), "Smoothed kitties do not match!")
  require(mirrored == readImage("/mirrored.bmp"), "Mirrored kitties do not match!")
}

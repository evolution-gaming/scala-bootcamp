package com.evolutiongaming.bootcamp.tf.shopping.util

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import scala.io.Source

object FileUtils {

  def writeToFile(fileName: String, content: String): Unit =
    Files.write(Paths.get(fileName), content.getBytes(StandardCharsets.UTF_8))

  def readFromFile(fileName: String): String = {
    val source  = Source.fromFile(fileName)
    val content = source.mkString
    source.close()
    content
  }

}

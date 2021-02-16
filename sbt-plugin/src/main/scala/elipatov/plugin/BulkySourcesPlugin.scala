package elipatov.plugin

import sbt._
import Keys._

import scala.util.Try

object BulkySourcesPlugin extends AutoPlugin {
  override def trigger = allRequirements

  object autoImport {
    val bulkyThresholdInLines = settingKey[Int]("Bulky sources threshold in lines")
    val bulkySources = taskKey[Seq[(Int, File)]]("Find sources greater than 'bulkyThresholdInLines' lines")
  }

  import autoImport._
  override lazy val globalSettings: Seq[Setting[_]] = Seq(
    bulkyThresholdInLines := 100,
  )

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    bulkySources := findBulkySources((Compile / sources).value, bulkyThresholdInLines.value),
    (Test / bulkySources) := findBulkySources((Test / sources).value, bulkyThresholdInLines.value)
  )

  private def findBulkySources(files: Seq[File], threshold: Int) = {
    val bulkyFiles =
      for {
        file <- files
        lines = Try(sbt.IO.readLines(file).size).getOrElse(-1)
        if lines >= threshold
      } yield (lines, file)

    bulkyFiles.sorted.reverse
  }
}

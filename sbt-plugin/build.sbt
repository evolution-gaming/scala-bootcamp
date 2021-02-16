ThisBuild / version := "0.1.0"
ThisBuild / organization := "elipatov"
ThisBuild / scalaVersion := "2.12.13"

lazy val root = (project in file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "bulky-sources",
    pluginCrossBuild / sbtVersion := {
      scalaBinaryVersion.value match {
        case "2.12" => "1.2.8" // set minimum sbt version
      }
    }
  )
name := "todo-list-fs"

scalaVersion := "2.13.7"

val http4sVersion = "0.23.6"
val circeVersion  = "0.14.1"

libraryDependencies += "org.typelevel" %% "cats-effect" % "3.3.0"
libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl"          % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-circe"        % http4sVersion,
)
libraryDependencies ++= Seq(
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser"  % circeVersion,
)
libraryDependencies += "co.fs2"        %% "fs2-io"      % "3.1.0"

enablePlugins(JavaAppPackaging)

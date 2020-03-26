name := "scala-bootcamp"

version := "0.1"

scalaVersion := "2.13.1"

// From https://tpolecat.github.io/2017/04/25/scalac-flags.html
scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-Ymacro-annotations"
)

val http4sVersion = "0.21.1"
val circeVersion = "0.13.0"
val playVersion = "2.8.1"
val akkaVersion = "2.6.3"
val doobieVersion = "0.8.8"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "2.1.0",
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "com.typesafe.akka" %% "akka-http" % "10.1.11",
  "de.heikoseeberger" %% "akka-http-circe" % "1.31.0",
  "com.typesafe.akka" %% "akka-stream" % "2.6.3",
  "io.chrisdavenport" %% "log4cats-slf4j" % "1.0.1",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "io.chrisdavenport" %% "epimetheus-http4s" % "0.3.0",
  "io.chrisdavenport" %% "cats-scalacheck" % "0.2.0" % Test,
  "org.scalatestplus" %% "scalatestplus-scalacheck" % "3.1.0.0-RC2" % Test,
  "org.typelevel" %% "simulacrum" % "1.0.0",
  "org.tpolecat" %% "atto-core" % "0.7.2",
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-generic-extras" % circeVersion,
  "io.circe" %% "circe-optics" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "org.tpolecat" %% "doobie-core" % doobieVersion,
  "org.tpolecat" %% "doobie-h2" % doobieVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "org.mockito" %% "mockito-scala" % "1.11.2" % Test,
  "org.scalaj" %% "scalaj-http" % "2.4.2" % Test,
  "org.tpolecat" %% "doobie-scalatest" % doobieVersion % Test,
)

addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full)

fork in run := true

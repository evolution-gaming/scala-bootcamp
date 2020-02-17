name := "scala-bootcamp"

version := "0.1"

scalaVersion := "2.13.1"

// From https://tpolecat.github.io/2017/04/25/scalac-flags.html
scalacOptions ++= Seq(
  "-Ymacro-annotations"
)

val http4sVersion = "0.21.0-RC2"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "2.1.0",
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,
  "io.chrisdavenport" %% "log4cats-slf4j" % "1.0.1",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "io.chrisdavenport" %% "epimetheus-http4s" % "0.3.0-RC7",
  "io.chrisdavenport" %% "cats-scalacheck" % "0.2.0" % Test,
  "org.scalatestplus" %% "scalatestplus-scalacheck" % "3.1.0.0-RC2" % Test,
  "org.typelevel" %% "simulacrum" % "1.0.0",
  "org.tpolecat" %% "atto-core" % "0.7.2",
  "io.circe" % "circe-core_2.13" % "0.13.0",
  "org.mockito" %% "mockito-scala" % "1.11.2" % Test,
)

addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full)

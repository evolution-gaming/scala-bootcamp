name := "scala-bootcamp"

version := "0.2"

scalaVersion := "2.13.8"

// From https://tpolecat.github.io/2017/04/25/scalac-flags.html
scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-Ymacro-annotations",
)

val http4sVersion = "0.21.22"
val circeVersion = "0.13.0"
val playVersion = "2.8.2"
val doobieVersion = "0.13.4"
val catsVersion = "2.6.1"
val catsTaglessVersion = "0.11"
val catsEffectVersion = "2.5.1"
val epimetheusVersion = "0.4.2"

val akkaVersion = "2.6.9"
val akkaHttpVersion = "10.1.11"
val akkaHttpCirceVersion = "1.31.0"

val log4CatsVersion = "1.1.1"

val scalaTestVersion = "3.2.7.0"
val h2Version = "2.0.202"
val slickVersion = "3.3.3"
val munitVersion = "0.7.29"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % catsVersion,
  "org.typelevel" %% "cats-effect" % catsEffectVersion,
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "org.http4s" %% "http4s-jdk-http-client" % "0.3.6",
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "de.heikoseeberger" %% "akka-http-circe" % akkaHttpCirceVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "io.chrisdavenport" %% "log4cats-slf4j" % log4CatsVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.codecommit" %% "cats-effect-testing-scalatest" % "0.4.1" % Test,
  "io.chrisdavenport" %% "epimetheus-http4s" % epimetheusVersion,
  "org.scalatestplus" %% "scalacheck-1-15" % scalaTestVersion % Test,
  "org.scalatestplus" %% "selenium-3-141" % scalaTestVersion % Test,
  "org.typelevel" %% "simulacrum" % "1.0.0",
  "org.tpolecat" %% "atto-core" % "0.8.0",
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-generic-extras" % circeVersion,
  "io.circe" %% "circe-optics" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-sharding" % akkaVersion,
  "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8",
  "org.scalameta" %% "munit" % munitVersion % Test,
  "org.typelevel" %% "munit-cats-effect-2" % "1.0.7" % Test,
  "org.tpolecat" %% "doobie-core" % doobieVersion,
  "org.tpolecat" %% "doobie-h2" % doobieVersion,
  "org.tpolecat" %% "doobie-hikari" % doobieVersion,
  "org.tpolecat" %% "doobie-munit" % doobieVersion % Test,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "org.mockito" %% "mockito-scala" % "1.16.32" % Test,
  "org.scalaj" %% "scalaj-http" % "2.4.2" % Test,
  "org.tpolecat" %% "doobie-scalatest" % doobieVersion % Test,
  "org.typelevel" %% "cats-tagless-macros" % catsTaglessVersion,
  "com.h2database" % "h2" % h2Version,
  "eu.timepit" %% "refined" % "0.9.17",
  "com.typesafe.slick" %% "slick" % slickVersion,
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
  "com.lihaoyi" %% "requests" % "0.6.5",
  "dev.zio" %% "zio" % "2.0.0-M4",
  "io.monix" %% "monix-reactive" % "3.4.0",
)

addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.13.2" cross CrossVersion.full)

run / fork := true
run / connectInput := true
run / outputStrategy := Some(StdoutOutput)

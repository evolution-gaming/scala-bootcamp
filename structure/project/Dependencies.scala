import sbt._

object Dependencies {
  val Actor     = "com.typesafe.akka" %% "akka-actor" % "2.5.32"
  val PlayJson  = "com.typesafe.play" %% "play-json"  % "2.9.2"
  val ScalaTest = "org.scalatest"     %% "scalatest"  % "3.2.11" % Test
  val All       = List(Actor, PlayJson, ScalaTest)
}

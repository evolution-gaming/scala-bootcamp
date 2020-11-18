import com.typesafe.sbt.packager.docker.{Cmd, ExecCmd}

name := "scala-bootcamp-containers"

version := "0.1"

scalaVersion := "2.13.3"

// From https://tpolecat.github.io/2017/04/25/scalac-flags.html
scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-Ymacro-annotations"
)

val catsEffectVersion = "2.2.0"
val catsVersion = "2.2.0"
val http4sVersion = "0.21.9"
val jakartaMailVersion = "2.0.0"
val logbackVersion = "1.2.3"
val pureconfigVersion = "0.14.0"
val redisClientVersion = "3.30"
val scalatestVersion = "3.2.3"

addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full)

lazy val containers = (project in file("."))
  .aggregate(integration, configInternal, configExternalBasic, configExternalAdvanced)

lazy val integration = (project in file("integration"))
  .configs(IntegrationTest)
  .settings(name := "basic")
  .settings(Defaults.itSettings)
  .settings(libraryDependencies ++= Seq(
    "jakarta.mail" % "jakarta.mail-api" % jakartaMailVersion,
    "com.sun.mail" % "jakarta.mail" % jakartaMailVersion,
    "net.debasishg" %% "redisclient" % redisClientVersion,
    "org.scalatest" %% "scalatest" % scalatestVersion % IntegrationTest
  ))

lazy val basicDockerSettings = Seq(
  dockerBaseImage := "adoptopenjdk:15",
  dockerUpdateLatest := true
  //  Docker / daemonGroup := "root",
  //  Docker / daemonUser := "root",
)

lazy val configShared = (project in file("config-shared"))
  .settings(name := "configShared")

lazy val configInternal = (project in file("config-internal"))
  .enablePlugins(DockerPlugin, JavaServerAppPackaging)
  .settings(basicDockerSettings)
  .settings(name := "configInternal")
  .settings(libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-core" % catsVersion,
    "org.typelevel" %% "cats-effect" % catsEffectVersion,
    "com.github.pureconfig" %% "pureconfig" % pureconfigVersion
  ))
  .dependsOn(configShared)

lazy val configExternalBasic = (project in file("config-external-basic"))
  .enablePlugins(DockerPlugin, JavaServerAppPackaging)
  .settings(basicDockerSettings)
  .settings(name := "configExternalBasic")
  .settings(libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-core" % catsVersion,
    "org.typelevel" %% "cats-effect" % catsEffectVersion,
    "com.github.pureconfig" %% "pureconfig" % pureconfigVersion
  ))
  .dependsOn(configShared)

lazy val configExternalAdvanced = (project in file("config-external-advanced"))
  .enablePlugins(DockerPlugin, JavaServerAppPackaging)
  .settings(basicDockerSettings)
  .settings(name := "configExternalAdvanced")
  .settings(libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-core" % catsVersion,
    "org.typelevel" %% "cats-effect" % catsEffectVersion,
    "com.github.pureconfig" %% "pureconfig" % pureconfigVersion
  ))
  .settings(
    Universal / mappings += {
      ((Default / baseDirectory).value / "conf/global.conf") -> "conf/global.conf"
    }
  )
  .dependsOn(configShared)

lazy val webServer = (project in file("web-server"))
  .enablePlugins(DockerPlugin, JavaServerAppPackaging)
  .settings(basicDockerSettings)
  .settings(name := "webServer")
  .settings(libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-core" % catsVersion,
    "org.typelevel" %% "cats-effect" % catsEffectVersion,
    "org.http4s" %% "http4s-blaze-server" % http4sVersion,
    "org.http4s" %% "http4s-dsl" % http4sVersion,
    "ch.qos.logback" % "logback-classic" % logbackVersion % Runtime
  ))
  .settings(
    dockerExposedPorts ++= Seq(10000, 5005),
    dockerCmd := Seq("-jvm-debug", "*:5005", "-Dlogback.logDir=/tmp/files", "-Dlogback.logFile=main"),
    dockerCommands := dockerCommands.value.flatMap {
      case ExecCmd("ENTRYPOINT", e) => Seq(
        Cmd("RUN", List("mkdir", "-p", "/tmp/files"): _*),
        Cmd("RUN", List("echo", java.util.UUID.randomUUID().toString, "|", "tee", "/tmp/files/uuid"): _*),
        ExecCmd("ENTRYPOINT", e)
      )
      case x => Some(x)
    }
  )
  .dependsOn(configShared)

fork in run := true

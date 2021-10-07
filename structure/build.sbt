ThisBuild / scalaVersion := "2.13.6"

ThisBuild / version := "1.0"

ThisBuild / libraryDependencies ++= Dependencies.All

lazy val root = project
  .in(file("."))
  .settings(
    name := "App Structure",
    normalizedName := "app-structure",
  )

/*

lazy val controllers = project
  .dependsOn(domain, infra, service, dto)

lazy val service = project
  .dependsOn(repos, infra)

lazy val repos = project
  .dependsOn(domain, infra)

lazy val domain = project
lazy val infra = project
lazy val dto = project

// boot, application
lazy val app = project
  .aggregate(user, group, permission, casino)
  // .dependsOn(user, group, permission, casino)
  // .dependsOn(user, casino)

lazy val user = project

lazy val group = project

lazy val permission = project

lazy val casino = project

// common
// assignment
 */

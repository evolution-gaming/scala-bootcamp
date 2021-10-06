scalaVersion := "2.13.6"

version := "1.0"

libraryDependencies ++= Dependencies.All

lazy val root = project
  .in(file("."))
  .settings(
    name := "App Structure",
    normalizedName := "app-structure",
  )

/*

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

lazy val root = (project in file(".")).dependsOn(assemblyPlugin)
lazy val assemblyPlugin = RootProject(file("../sbt-plugin"))
//addSbtPlugin("elipatov.plugin" % "bulky-sources" % "0.1.0")
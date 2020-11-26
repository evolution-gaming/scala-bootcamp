# Changing existing project

Conventions are all good and fine, but what if we want to change the defaults?
The good style is to avoid doing it, to keep our project similar to others, but
sometimes we have to do the changes. The good test is to only change what is
actually _different_ in this specific project from all other projects in the
world.

How do one mutates the default settings and tasks in sbt? The quick answer: you
cannot change them. What is hardcoded in sbt, it there forever. As often in
functional programming, everything is immutable in sbt.

What you can do is to provide `sbt` a set of functions that will _transform_
the internal map of the settings and tasks to something else in special `build.sbt`
file.

Why such ceremony is required? Because it allows sbt to detect the required
sequence of build steps automatically and execute as much things as possible
in parallel.

Let's have a look on the syntax of `build.sbt` file in a simple example.

If you ever seen `build.sbt` before you might have noticed some strange
operators there like `:=`, but in reality `build.sbt` uses Scala as its
syntax, no XML, no JSON and no YAML. These operators are just convinience
functions or macros made in Scala. Everything is compiled and errors are
reported.

Remember `build.properties` file where `sbt.version` was added to keep builds
reproducible. For the same reason, you often want to set Scala version.
How one does it in `build.sbt`?

Add the following line into `build.sbt` in our `5-inspect` project:
```
scalaVersion := "2.13.4"
```
Here we have `scalaVersion`, which is a setting key, `:=` which is an operator,
and `"2.13.4"` which is a definition body.

If you have sbt running from a previous session, you will need to reload
`build.sbt` so the changes are take into account.

Let's have a look at our new Scala version now and then inspect it:
```
sbt:root-5-inspect> scalaVersion
sbt:root-5-inspect> inspect scalaVersion
```
What do you have in `Defined at:` section? Do you see your `build.sbt` file
there?

What did we just do? By using `:=` syntax we told to sbt to find the setting
with a name `scalaVersion` and apply the value to it.

Can we put an arbitrary code into `scalaVersion`? Let's do it and change our
line to the following to surprise the maintainers of our project:
```
scalaVersion := {
  val version = (math.random() * 5).toInt
  s"2.13.$version"
}
```
Now run it in your sbt project several times:
```
reload
scalaVersion
scalaVersion
scalaVersion
```
What do you see? Why?

Let's have a bit more fun. Let's override `run` task and have our "Hello world"
application using sbt.

Put the line into `build.sbt` and run it using `reload` and `run`.
```
run := println("Hello World!")
```

What if we want to make one setting or task depend on another? We can use
special `.value` macro for that in settings and simple tasks
and `.evaluated` for input tasks.

I.e. like this:
```
run := println(s"Project version: ${version.value}")
```

Excercise: define `run` task which outputs current scala version.

# Scopes

As you may have noticed when using `inspect` command, sbt does not only have the
keys, it also have the scope for these keys. I.e. every task or setting could be
be also in the scope.

Let's see what it means in practice:
```
sbt:root-5-inspect> show Compile / sourceDirectories
sbt:root-5-inspect> show Test / sourceDirectories
```
Do you see the difference?

All the setting keys could actually be scoped to: project, to configuration,
and to task.

I.e. the full name of the setting could be the following, actually:
```
root-5-inspect / Compile / run / scalaVersion
```

It means, roughly, that this is `scalaVersion`, which will be used when executing
`run` task in `Compile` scope within `root-5-inspect` project. Actually, the `run`
task we used is defined inside of `Compile` scope.

The reason why we could run it, is that there are delegation rules of where the
task should be searched for, if it is not found in the current scope.

We will not discuss it right now, but you can read about it here:
https://www.scala-sbt.org/1.x/docs/Scope-Delegation.html#Scope+delegation+rules

The important thing to remember: if `root-5-inspect / Compile / run / scalaVersion`
is defined, and you will change `root-5-inspect / Compile / scalaVersion` or
`root-5-inspect / scalaVersion`, the one under `run` will not be affected.

This is a _very_ common issue people encounter, especially with not-so-well
written builds or plugins. `inspect` will always help you to figure out what
went wrong.

Let's redefine our `run` to be `Compile / run`:
```
Compile / run := println(s"Project version: ${version.value}")
```
Actually we can use the same scoping syntax when referring the values:
```
Compile / run := {
  val projectVersion = (Compile / version).value
  println(s"Project version: $projectVersion")
}
```
The interesting part is that, when we refer to `.value` or `.evaluated`, we do
not specify the order. I.e. we can write something like this:
```
(Compile / compile).value
(Compile / run).value
```
And this does not guarantee anyhow that the first task will run before a second
one. This is a mechanism behind ability of sbt to run the tasks in parallel.
It _will_ run everything it can in paralell.

Excercise: define `Compile / run` task that:

1. Prints used Scala version when started (consider using `scalaVersion.value`).
2. Runs a normal `Compile / run` task (consider using `(Compile / run).evaluated`).
3. Prints the time when the task was finished (consider using `java.time.Instant.now()`).

# Projects

What is that `root-5-inspect` we mentioned before and see all the time in our
sbt interactive console? This is a default project id.

In real life projects, this may often be redefined like this:
```
lazy val root = (project in file("."))
  .settings(
    name := "inspect",
    scalaVersion := "2.13.4",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "2.2.0",
      "org.scalatest" %% "scalatest" % "3.2.2" % "test"
    )
  )
```
Why would anyone want to do this? Actually, if we do not write `root` project
then `sbt` will genereate the one for us, silently and use our settings to pass
to the project.

The key part is that allows us defining several projects in one `build.sbt` file
(see also https://www.scala-sbt.org/1.x/docs/Multi-Project.html). Sometimes people
also call such projects "modules".

One cool feature that you can do when having multiple modules is _different_
set of libraries you depend on. I.e. you could put your business logic into
really clean project without any dependencies and this will protect you against
accidenital usage of the library.

Let's define the new structure for our `inspect` project:
```
lazy val root = (project in file("."))
  .aggregate(domain, services)
  .dependsOn(services)
  .settings(name := "inspect")

lazy val services = (project in file("services"))
  .dependsOn(domain)
  .settings(
    name := "inspect-services",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "2.2.0",
      "org.scalatest" %% "scalatest" % "3.2.2" % "test"
    )
  )

lazy val domain = (project in file("domain"))
  .settings(
    name := "inspect-domain",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.2.2" % "test"
    )
  )
```
Live coding session: refactor `inspect` project so text from `Printer` goes to
`domain` and everything else goes to `services`, and it still works.

# Plugins

How do the people reuse the parts of sbt definitions between the different code bases?

One way is to use predefined plugins created by other people. Let's add code
coverage plugin to our sbt project: https://github.com/scoverage/sbt-scoverage

For that, create `plugins.sbt` file in your `project` directory and add the
following line:
```
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.6.1")
```

This is it. Let's try to run a new plugin in our sbt session:
```
sbt:inspect> clean; coverage; test; coverageReport
```

# Common code

Another, common, way is to put `.scala` files to `project` directory.

Live coding session: move all library dependencies into
`project/Dependencies.scala`.

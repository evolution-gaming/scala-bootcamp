# Inspect the exisiting project

We will now investigate how sbt knew where to take the files from, where to take
the libraries for running etc. For that we will use another, `5-inspect` project.
It is not much different from previous projects, just a bit larger and more
production-ready `src/main/scala` directory is used to store sources.

As it was told, sbt is using "Convention over Configuration" principle. Roughly
speaking sbt is just a set of scoped *tasks* and *settings* that you can either
run directly or manipulate. The difference between a task and a setting is that
the task will execute something when each time it is run, but the setting is
calculated once during the load.

Let's see what does it mean in practice by inspecting the built-in conventions
ourselves.

Let's run sbt in our new project:
```
# assuming you are at `scala-bootstrap/sbt`
cd 5-inspect

# let's run sbt console
sbt

# let's see our project works in sbt as previously by executing `run` task
sbt:root-5-inspect> run

# now, let's find out where sbt looks for sources in this project by checking
# `sourceDirectories` setting
sbt:root-5-inspect> sourceDirectories

# how about finding out where it puts resulting files by checking another setting?
sbt:root-5-inspect> target

# this is not fun though, I was cheating, because I knew the names of the propreties
# what is we do not know the names, and cannot find them in documentation?
#
# let's dissect a `run` command a little bit:
sbt:root-5-inspect> inspect run

# there are two very interesting places, one is `Defined at:`
#
# in my case it points to the following place:
# https://github.com/sbt/sbt/blob/v1.4.2/main/src/main/scala/sbt/Defaults.scala#L907
#
# another is `Dependencies:` with the tasks this one runs
#
# we can discover that it actually runs `bgRun`
#
sbt:root-5-inspect> inspect bgRun

# oh, there is something interesting, it is `mainClass`, let's run it
bt:root-5-inspect> mainClass

# nothing! let's try to find out why:
bt:root-5-inspect> inspect mainClass

# now we know why!
# `mainClass` is not a setting, it is a task
# the reason: sometimes you cannot know `mainClass` until you compile the project
# if we do this, we will actually find that `mainClass` depends on `compile`
bt:root-5-inspect> inspect discoveredMainClasses
```

As a final task in this section, use `inspect run` and then `inspect` and `show`
on dependencies to find out the name of the task used to get full list of JARs
used to run our application (also called a classpath). Post the name and results
of `show` command to Slack chat.

Given this is not your first lecture on Scala, you, probably, have sbt setup and
working for quite some time.

We will reiterate some steps just in case. You will need to have sbt installed
before this lecture.

One way is to follow the official sbt documentation here:
https://www.scala-sbt.org/1.x/docs/Setup.html

Another (which I, personally, use) is to use Coursier (which is a Maven-style
dependency fetcher for Scala). You can install it as described here:
https://get-coursier.io/docs/cli-installation

And then just use `cs install sbt` to get sbt working. You can check it by
launching `sbt --version`. The reason why I like coursier is that it also
allows installing Scala REPL (`cs install scala`) and other tools, and
switch between Java versions out of the box.

Note, that Coursier sbt launcher differs a little bit from the official one
and may not support some commands yet, such as `sbt --client`, which will not
be needed for this workshop.

For sake of having all the students the same sbt version, make sure you have
`1.4.2` version when running `sbt --version`.

For introduction part you will also need `scalac` installed. If you do not like
installing stuff on your computer, it is not strictly required. You will just
miss couple of introductory excersises if you do not.

If you used coursier, as suggested above, it is about running one command only:
`cs install scalac` and you should have it working. You can remove it as easily
by running `cs uninstall scalac` after the lecture.

You can check if it works by running `scalac --version`. Post results of running
both `sbt` and `scalac` commands into the chat.

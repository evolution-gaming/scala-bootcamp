# Setup sbt

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

# Setup scala and scalac

For introduction part you will also need `scalac` and `scala` installed. These
are only needed for  couple of introductory excersises. These are not required
for normal sbt work.

If you used coursier, as suggested above, it is about running two command only
and you should have it working:
```
cs install scala
cs install scalac
```
You can remove it as easily by running the following after the lecture:
```
cs uninstall scala
cs uuinstall scalac
```

You can check if everything works by running the following commands:
```
sbt --version
scala --version
scalac --version
```
Post results of running all these commands into the chat.

*Do not forget to do `git pull` just before the lecture!*


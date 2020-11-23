How do the one compile Scala (or Java, or C) file without a built tool. It is
quite easy to do. If you have a compiler, you can do it directly. Let's compile
the application in `introduction` directory directly as an excersise:
```
# assuming you are at `scala-bootstrap/sbt`
cd introduction

# let's compile the stuff now
# (run `scalac --help` if you want to know more)
scalac Introduction.scala

# let's run it! (why we are not using `.scala` at the end?)
# (run `scala --help` if you want to know more)
scala Introduction

# it wasn't so hard, may be no need for build tools?
# let' see what your compiler generated:
# (use `dir` instead of `ls -l` on Windows)
ls -l

# wow, what are these files? let's clean them up
# (use `del` or your File Explorer on Windows)
rm *.class
```

What if we need to use the libraries to run our code? Let's try to compile the
project without adding libraries first.
```
# assuming you are at `scala-bootstrap/sbt`
cd libraries

# let's try compile the stuff now (there will be an error)
scalac Libraries.scala
```

Got errors? We need cats-effect. Where we take the artifact?
Let's go Maven central: https://search.maven.org/
Search for `cats-effect_2.13-2.2.0.jar` and press `Download` arrow
at the right of the row.

Select `jar`, download it and put it to `scala-bootstrap/sbt/libraries` near
`Libraries.scala` file.
```
scalac Libraries.scala

# Hurray! We have it compiled! Let's try to run it now:
scala Libraries

# I got an error? Why did I? How to fix it?
```

This felt like a real pain to me. Did it feel to to you? The real Scala or Java
applications may have hundreds of dependencies. Downloading them manually,
copying in the right place and running could be a huge pain without the appropriate
tools.

There are plenty of the builds tools in the programming world. One of most known
is, arugable, `make` and derivatives, which is very well known to `C/C++` crowd.

In JVM world, one of the first very popular tools was
[`Apache Ant`](https://ant.apache.org/), which allowed developers to specify the
commands to execute using, then popular, prescriptive XML format. I.e. you would
say to Ant where to take the files and where to put them like this:
```
<copy file="myfile.txt" todir="../some/other/dir"/>
```

The presecriptive tools went out of fashion, because the people tend to create
the very different projects, without own conventions etc., which are hard to
follow and reuse outside of specific teams.

One of the first and very popular tool was Maven, which I mentioned before.

To quote wikipedia:
> The word itself is a borrowing from the Yiddish מבֿין meyvn 'an expert,
connoisseur', derived from the Hebrew מבין‎ mēvīn 'person with understanding,
teacher', a participle of the verb הֵבִין‎ hēvīn '(he) understood'...

The idea was to use "Convention over Configuration" principle a lot and make
sure most of the projects use the same structure. Then compiling, running,
testing etc. the code would be about putting it to the right directories.

`sbt`, which used to mean "Simple Build Tool` is a continuation of this
tradition. Despite the name, `sbt` used to be really hard to use and understand
with awkwards symbols, conventions and ideas. This is _not_ the case anymore,
it is really simplified and easy to use these days but I am sharing this,
because you might encounter such an opinion based on an experience of old years.

Because it was so infamous, and out of strive to innovate, other tools appeared
in Scala word since that. Most people still use sbt though.
- http://www.lihaoyi.com/mill/ by Li Haoyi, from Databricks, whom I mentioned before,
- https://github.com/propensive/fury by Jon Pretty from Propensive, a famous and
  very respected Scala developer.

In large mixed corporate environments where Scala is not the only backend
language, often other, not Scala specific tools are used:
- https://maven.apache.org/ the original XML reading beast by ASF,
- https://gradle.org/ popular modern build tool by Gradle Inc.,
- https://bazel.build/ scalable polyglot tool to build huge repositories by Google.

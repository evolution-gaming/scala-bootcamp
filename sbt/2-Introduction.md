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

There are plenty of the builds tools in the programming world. One of most known
is, arugable, `make` and derivatives, which is very well known to `C/C++` crowd.

In JVM world, one of the first very popular tools was
[`Apache Ant`](https://ant.apache.org/), which allowed developers...

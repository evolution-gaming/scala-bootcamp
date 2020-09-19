# Scala Bootcamp

This repository contains information on the [Evolution Gaming](https://eng.evolutiongaming.com/) Scala Bootcamp.

## Schedule

The [2020 Q3-Q4 bootcamp](https://scala-bootcamp.evolutiongaming.com/) has the following tentative schedule:

| Date       | Title                                 | Description                                                                                           | Responsible                                  | Materials  |
|------------|---------------------------------------|-------------------------------------------------------------------------------------------------------|----------------------------------------------|------------|
| 2020-09-22 | Introduction and Basic Syntax         | Introduction to bootcamp, introduction to Scala syntax                                                | [@jurisk](https://github.com/jurisk)         | [Basics](src/main/scala/com/evolutiongaming/bootcamp/basics/Basics.scala) ([t](src/test/scala/com/evolutiongaming/bootcamp/basics/BasicsSpec.scala)), [Classes & Traits](src/main/scala/com/evolutiongaming/bootcamp/basics/ClassesAndTraits.scala) ([t](src/test/scala/com/evolutiongaming/bootcamp/basics/ClassesAndTraitsSpec.scala)) |
| 2020-09-24 | Control Structures                    | `if`-`else`, recursion, `map`, `flatMap`, `filter`, `for`-comprehensions                              | [@jurisk](https://github.com/jurisk)         | [Control Structures](src/main/scala/com/evolutiongaming/bootcamp/basics/ControlStructures.scala) ([t](src/test/scala/com/evolutiongaming/bootcamp/basics/ControlStructuresSpec.scala)) |
| 2020-09-29 | Data Structures & Functions           | Mutable vs. immutable, `Array`-s, `List`-s, `Map`-s, tuples, more on functions                        | [@jurisk](https://github.com/jurisk)         | [Data Structures](src/main/scala/com/evolutiongaming/bootcamp/basics/DataStructures.scala) ([t](src/test/scala/com/evolutiongaming/bootcamp/basics/DataStructuresSpec.scala)), [Functions](src/main/scala/com/evolutiongaming/bootcamp/functions/Functions.scala) ([t](src/test/scala/com/evolutiongaming/bootcamp/functions/FunctionsSpec.scala)) |
| 2020-10-01 | Algebraic Data Types                  | ADTs, their role in functional design, implementing and using them in Scala                           | [@apavlovics](https://github.com/apavlovics) | [ADTs](src/main/scala/com/evolutiongaming/bootcamp/adt/AlgebraicDataTypes.scala) ([t](src/test/scala/com/evolutiongaming/bootcamp/adt/AlgebraicDataTypesSpec.scala)) |
| 2020-10-06 | Implicits & Type Classes              | Implicits, type classes, defining them in Scala, Higher Kinded Types                                  | [@migesok](https://github.com/migesok)       | | 
| 2020-10-08 | Questions & Answers                   | Covering gaps - Q&A on subjects covered so far, design & coding practice                              | [@jurisk](https://github.com/jurisk)         | | 
| 2020-10-13 | Error Handling                        | `Option`, `Either`, `Validated`, `Try`, `Future`, `IO`, encoding errors as ADTs                       | [@apavlovics](https://github.com/apavlovics) | |
| 2020-10-15 | Unit Testing                          | Benefits, testing pyramid, ScalaTest, structuring code to be testable                                 | [@rtar](https://github.com/rtar)             | |
| 2020-10-20 | Processing JSON                       | Processing JSON using Circe, writing custom coders and decoders                                       | [@arixmkii](https://github.com/arixmkii)     | [JSON](src/main/scala/com/evolutiongaming/bootcamp/json/CirceExercises.scala) ([t](src/test/scala/com/evolutiongaming/bootcamp/json/CirceExercisesSpec.scala), [hw](src/test/scala/com/evolutiongaming/bootcamp/json/HomeworkSpec.scala)) |
| 2020-10-22 | Cats                                  | `cats-core` introduction, Monad Transformers                                                          | [@Nbooo](https://github.com/Nbooo)           | |
| 2020-10-27 | Questions & Answers                   | Covering gaps - Q&A on subjects covered so far, design & coding practice                              | [@jurisk](https://github.com/jurisk)         | | 
| 2020-10-29 | Variances                             | Understanding variances: covariance, contravariance, invariance                                       | [@saraiva132](https://github.com/saraiva132) | |
| 2020-11-03 | Asynchronous Programming              | Classic JVM threading, critical sections using `synchronized` and their perils, `Atomic*`             | [@Demosfen92](https://github.com/Demosfen92) | |
| 2020-11-05 | Asynchronous Effects                  | Asynchronous Effects, e.g. Cats Effect IO and/or ZIO                                                  | [@Demosfen92](https://github.com/Demosfen92) | |
| 2020-11-10 | Shared State in FP                    | Using `Ref`-s and `MVar`-s                                                                            | [@Demosfen92](https://github.com/Demosfen92) | |
| 2020-11-12 | HTTP and Web Sockets                  | Akka HTTP and/or http4s for HTTP REST and Web Sockets                                                 | [@apavlovics](https://github.com/apavlovics) | |
| 2020-11-17 | Akka                                  | Akka Actors and other Akka features                                                                   | [@migesok](https://github.com/migesok)       | |
| 2020-11-19 | Structure of Scala Applications       | SBT, single- vs multi-module projects, "package-by-feature"                                           | [@rtar](https://github.com/rtar)             | |
| 2020-11-24 | Containers                            | Docker and Kubernetes                                                                                 | [@arixmkii](https://github.com/arixmkii)     | |
| 2020-11-26 | Working with Databases                | Brief overview of using databases from Scala: SQL, JDBC, Slick, Doobie                                | [@mr-git](https://github.com/mr-git)         | [Databases](src/main/scala/com/evolutiongaming/bootcamp/db/DoobieExercises.scala) |
| 2020-12-01 | Event Sourcing using Akka Persistence | Event Sourcing, implementing it using Akka Persistence, CQRS                                          | [@mikla](https://github.com/mikla)           | |
| ...        | Work on Course Projects               | Participants work on course projects, Q&A sessions - joint or with mentors                            | N/A                                          | |
| 2020-12-15 | Presentation of Course Projects       | Participants present course projects to mentors & each other                                          | [@jurisk](https://github.com/jurisk)         | |
| 2020-12-17 | Presentation of Course Projects       | Participants present course projects to mentors & each other - continued                              | [@jurisk](https://github.com/jurisk)         | |
| 2020-12-22 | Development Practices & Processes     | Role of processes, Agile, Code Reviews, Testing, CI/CD                                                | [@jurisk](https://github.com/jurisk)         | |
| 2020-12-29 | Graduation                            | Discussion of course results, free form Q&A session                                                   | [@jurisk](https://github.com/jurisk)         | |

The schedule will be adjusted according to learning progress.

## Prerequisites

Please install recent versions of the following before the first lecture:
- [intelliJ IDEA Community Edition](https://www.jetbrains.com/idea/download/)
- [Scala plug-in](https://www.jetbrains.com/help/idea/discover-intellij-idea-for-scala.html) for IntelliJ IDEA
- OpenJDK for the relevant platform (e.g., [Windows / Linux](https://jdk.java.net/14/) or [MacOS homebrew](https://formulae.brew.sh/formula/openjdk))
- [Scala](https://www.scala-lang.org/download/)
- [SBT](https://www.scala-sbt.org/download.html)
- [Git](https://git-scm.com/downloads)
- Check out the [Scala Bootcamp](https://github.com/evolution-gaming/scala-bootcamp) project and ensure you can run the tests for the project. The tests will fail (for now), this is normal and expected.

Alternatives that are also expected to work:
- [VS Code with Metals](https://marketplace.visualstudio.com/items?itemName=scalameta.metals) as an IDE
- Oracle JDKs

In case of issues:
- Read [Getting Started with Scala](https://docs.scala-lang.org/getting-started/index.html)
- Read [Discover IntelliJ IDEA for Scala](https://www.jetbrains.com/help/idea/discover-intellij-idea-for-scala.html)
- Ask in the Bootcamp chat

## Learning resources

All resources are listed in no particular order.

### Books

- [Essential Scala](https://underscore.io/books/essential-scala/) (free)
- [Scala with Cats 2](https://www.scalawithcats.com/) (free)
- [Functional Programming in Scala](https://www.manning.com/books/functional-programming-in-scala#toc) (free)
- [Scala from Scratch: Exploration](https://leanpub.com/scala-from-scratch-exploration) ($15+)
- [Functional Programming for Mortals](https://leanpub.com/fpmortals-cats) ($15+)
- [Practical FP in Scala: A hands-on approach](https://leanpub.com/pfp-scala) ($30+)
- [Programming in Scala](https://booksites.artima.com/programming_in_scala_3ed) ($30+)

### Other

- [Tour of Scala](https://docs.scala-lang.org/tour/tour-of-scala.html) & [Scala Book](https://docs.scala-lang.org/overviews/scala-book/introduction.html) from [scala-lang.org](https://www.scala-lang.org/)
- [Scala Exercises](https://www.scala-exercises.org/) 
- [Coursera Scala Specialization](https://www.coursera.org/specializations/scala)

### Non-Scala

- [Learn Git Branching](https://learngitbranching.js.org/)

## Status

* The [2020 Q1-Q2 bootcamp](https://evolution-gaming.timepad.ru/event/1106949/) has concluded
* The [2020 Q3-Q4 bootcamp](https://scala-bootcamp.evolutiongaming.com/) is ongoing

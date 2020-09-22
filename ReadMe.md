# Scala Bootcamp

This repository contains information on the [Evolution Gaming](https://eng.evolutiongaming.com/) Scala Bootcamp.

## Schedule

The [2020 Q3-Q4 bootcamp](https://scala-bootcamp.evolutiongaming.com/) has the following tentative schedule:

| 2020-*     | Topics                                                                                                      | Responsible                                  | Materials                                                                                                                                                                                                                                                                                                                                          |
|------------|-------------------------------------------------------------------------------------------------------------|----------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 09-22      | **Introduction** and **basic Scala Syntax** (types, functions, parametric polymorphism)                     | [@jurisk](https://github.com/jurisk)         | [Basics](src/main/scala/com/evolutiongaming/bootcamp/basics/Basics.scala) ([t](src/test/scala/com/evolutiongaming/bootcamp/basics/BasicsSpec.scala))                                                                                                                                                                                               |
| 09-24      | **Classes & Traits** and some **Control Structures**                                                        | [@jurisk](https://github.com/jurisk)         | [Classes & Traits](src/main/scala/com/evolutiongaming/bootcamp/basics/ClassesAndTraits.scala) ([t](src/test/scala/com/evolutiongaming/bootcamp/basics/ClassesAndTraitsSpec.scala))                                                                                                                                                                 |
| 09-29      | **Control Structures**: `if`-`else`, recursion, `map`, `flatMap`, `filter`, `for`-comprehensions            | [@jurisk](https://github.com/jurisk)         | [Control Structures](src/main/scala/com/evolutiongaming/bootcamp/basics/ControlStructures.scala) ([t](src/test/scala/com/evolutiongaming/bootcamp/basics/ControlStructuresSpec.scala))                                                                                                                                                             |
| 10-01      | **Data Structures** ([im]mutable, `Array`, `List`, `Map`, tuples) & **Functions** ([im]pure, total/partial) | [@jurisk](https://github.com/jurisk)         | [Data Structures](src/main/scala/com/evolutiongaming/bootcamp/basics/DataStructures.scala) ([t](src/test/scala/com/evolutiongaming/bootcamp/basics/DataStructuresSpec.scala)), [Functions](src/main/scala/com/evolutiongaming/bootcamp/functions/Functions.scala) ([t](src/test/scala/com/evolutiongaming/bootcamp/functions/FunctionsSpec.scala)) |
| 10-06      | **Algebraic Data Types** - their role in functional design, implementing and using them in Scala            | [@apavlovics](https://github.com/apavlovics) | [ADTs](src/main/scala/com/evolutiongaming/bootcamp/adt/AlgebraicDataTypes.scala) ([t](src/test/scala/com/evolutiongaming/bootcamp/adt/AlgebraicDataTypesSpec.scala))                                                                                                                                                                               |
| 10-08      | **Implicits** & **Type Classes** - defining them in Scala, Higher Kinded Types                              | [@migesok](https://github.com/migesok)       |                                                                                                                                                                                                                                                                                                                                                    |
| 10-13      | **Questions & Answers** - covering gaps, design & coding practice                                           | [@jurisk](https://github.com/jurisk)         |                                                                                                                                                                                                                                                                                                                                                    |
| 10-15      | **Error Handling** - `Option`, `Either`, `Validated`, `Try`, `Future`, `IO`, encoding errors as ADTs        | [@apavlovics](https://github.com/apavlovics) |                                                                                                                                                                                                                                                                                                                                                    |
| 10-20      | **Unit Testing** - Benefits, testing pyramid, ScalaTest, structuring code to be testable                    | [@rtar](https://github.com/rtar)             |                                                                                                                                                                                                                                                                                                                                                    |
| 10-**23**  | **Processing JSON** using Circe, writing custom coders and decoders                                         | [@arixmkii](https://github.com/arixmkii)     | [JSON](src/main/scala/com/evolutiongaming/bootcamp/json/CirceExercises.scala) ([t](src/test/scala/com/evolutiongaming/bootcamp/json/CirceExercisesSpec.scala), [hw](src/test/scala/com/evolutiongaming/bootcamp/json/HomeworkSpec.scala))                                                                                                          |
| 10-27      | **Cats** - `cats-core` introduction, Monad Transformers                                                     | [@Nbooo](https://github.com/Nbooo)           |                                                                                                                                                                                                                                                                                                                                                    |
| 10-29      | **Questions & Answers** - covering gaps, design & coding practice                                           | [@jurisk](https://github.com/jurisk)         |                                                                                                                                                                                                                                                                                                                                                    |
| 11-03      | **Asynchronous Programming** - JVM threads, perils of critical sections using `synchronized`, `Atomic*`     | [@Demosfen92](https://github.com/Demosfen92) |                                                                                                                                                                                                                                                                                                                                                    |
| 11-05      | **Asynchronous Effects** - e.g. Cats Effect IO and/or ZIO, `Fiber`-s                                        | [@Demosfen92](https://github.com/Demosfen92) |                                                                                                                                                                                                                                                                                                                                                    |
| 11-10      | **Shared State in FP** - Using `Ref`-s and `MVar`-s                                                         | [@Demosfen92](https://github.com/Demosfen92) |                                                                                                                                                                                                                                                                                                                                                    |
| TBD        | **Fibers**                                                                                                  | [@saraiva132](https://github.com/saraiva132) |                                                                                                                                                                                                                                                                                                                                                    |
| 11-12      | **HTTP and Web Sockets** - Akka HTTP and/or http4s for HTTP REST and Web Sockets                            | [@apavlovics](https://github.com/apavlovics) |                                                                                                                                                                                                                                                                                                                                                    |
| 11-17      | **Akka** - Actors and other Akka features                                                                   | [@migesok](https://github.com/migesok)       |                                                                                                                                                                                                                                                                                                                                                    |
| 11-19      | **Structure of Scala Applications** - SBT, single- vs multi-module projects, "package-by-feature"           | [@rtar](https://github.com/rtar)             |                                                                                                                                                                                                                                                                                                                                                    |
| 11-24      | **Containers** - Docker and Kubernetes                                                                      | [@arixmkii](https://github.com/arixmkii)     |                                                                                                                                                                                                                                                                                                                                                    |
| 11-26      | **Working with Databases** from Scala: JDBC, Slick, Doobie                                                  | [@mr-git](https://github.com/mr-git)         | [Databases](src/main/scala/com/evolutiongaming/bootcamp/db/DoobieExercises.scala)                                                                                                                                                                                                                                                                  |
| 12-01      | **Event Sourcing using Akka Persistence** and CQRS                                                          | [@mikla](https://github.com/mikla)           |                                                                                                                                                                                                                                                                                                                                                    |
| ...        | **Work on Course Projects** and Q&A sessions - joint or with mentors                                        | N/A                                          |                                                                                                                                                                                                                                                                                                                                                    |
| 12-15      | **Presentation of Course Projects** to mentors & each other                                                 | [@jurisk](https://github.com/jurisk)         |                                                                                                                                                                                                                                                                                                                                                    |
| 12-17      | **Presentation of Course Projects** to mentors & each other - continued                                     | [@jurisk](https://github.com/jurisk)         |                                                                                                                                                                                                                                                                                                                                                    |
| 12-22      | **Development Practices & Processes** - Agile, Code Reviews, Testing, CI/CD                                 | [@jurisk](https://github.com/jurisk)         |                                                                                                                                                                                                                                                                                                                                                    |
| 12-29      | **Graduation** - Discussion of course results, free form Q&A session                                        | [@jurisk](https://github.com/jurisk)         |                                                                                                                                                                                                                                                                                                                                                    |

The schedule will be adjusted according to learning progress.

## Preparation for the bootcamp

### Prerequisites

Please install recent versions of the following before the first lecture:
- [intelliJ IDEA Community Edition](https://www.jetbrains.com/idea/download/)
- [Scala plug-in](https://www.jetbrains.com/help/idea/discover-intellij-idea-for-scala.html) for IntelliJ IDEA
- OpenJDK, e.g. [AdoptOpenJDK](https://adoptopenjdk.net/), [Oracle OpenJDK](https://jdk.java.net/) or [OpenJDK using homebrew](https://formulae.brew.sh/formula/openjdk) (for MacOS)
- [Scala](https://www.scala-lang.org/download/)
- [SBT](https://www.scala-sbt.org/download.html)
- [Git](https://git-scm.com/downloads)

Alternatives that are also expected to work:
- [Visual Studio Code with Metals](https://marketplace.visualstudio.com/items?itemName=scalameta.metals) as an IDE

### Prepare the project

- Check out the [Scala Bootcamp](https://github.com/evolution-gaming/scala-bootcamp) project
- Run tests from the command line using `sbt test`
- Open the project in IntelliJ IDEA and run tests there (right-click on `scala-bootcamp` project in the left panel and click `Run ScalaTests in ‘scala…’`)

The tests will fail (for now), this is normal and expected.

### Troubleshooting

In case of issues:
- Read [Getting Started with Scala](https://docs.scala-lang.org/getting-started/index.html)
- Read [Discover IntelliJ IDEA for Scala](https://www.jetbrains.com/help/idea/discover-intellij-idea-for-scala.html)
- Ask in the Bootcamp chat

## Learning resources

All resources are listed in no particular order.

### Books

- [Essential Scala](https://underscore.io/books/essential-scala/) (free)
- [Scala with Cats 2](https://www.scalawithcats.com/) (free)
- [Scala from Scratch: Exploration](https://leanpub.com/scala-from-scratch-exploration) ($15+)
- [Functional Programming in Scala](https://www.manning.com/books/functional-programming-in-scala#toc) ($25+)
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

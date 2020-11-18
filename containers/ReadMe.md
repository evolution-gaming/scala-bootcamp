# Scala Bootcamp

This repository contains information on the [Evolution](https://eng.evolution.com/) Scala Bootcamp.

## Containers

This is the root location for materials for Containers/K8s/Clustering lessons.

## Prepare for this lecture

This lecture will require additional preparations as it is not Scala only focused.
This lecture should provide useful insight how containerized applications could be used during development and deployment
of Scala applications, but will take a significant step aside to build needed foundation, before one can apply it in
their job.

## Preparation for the bootcamp

### Prerequisites

Please install recent versions of the following before the first lecture:
- [intelliJ IDEA Community Edition](https://www.jetbrains.com/idea/download/)
- [Scala plug-in](https://www.jetbrains.com/help/idea/discover-intellij-idea-for-scala.html) for IntelliJ IDEA
- OpenJDK, e.g. [AdoptOpenJDK](https://adoptopenjdk.net/), [Oracle OpenJDK](https://jdk.java.net/) or [OpenJDK using homebrew](https://formulae.brew.sh/formula/openjdk) (for MacOS)
- [Scala](https://www.scala-lang.org/download/)
- [SBT](https://www.scala-sbt.org/download.html)
- [Git](https://git-scm.com/downloads)
- [Docker](https://www.docker.com/products/docker-desktop)
- [Minikube](https://minikube.sigs.k8s.io/docs/start/)

Alternatives that are also expected to work:
- [Docker Machine](https://docs.docker.com/machine/) with [VirtualBox](https://www.virtualbox.org/) can be used as alternative to Docker for Desktop
- [Visual Studio Code with Metals](https://marketplace.visualstudio.com/items?itemName=scalameta.metals) as an IDE

### Installing Docker and Minikube

#### On Windows 10 (with WSL2)

*WSL2 is the only backend we tested.* If you plan to use a different backend, you should prepare to fix some rough edges
yourself.

TBD (separate document with links and description)

#### On MacOS

TBD (separate document with links and description)

#### On Linux

Follow the instructions for your distro of choice as there might be significant differences between Linux flavors.
If `podman` is considered a better supported option for your Linux, you can use it with docker command aliases.

#### Using Docker Machine

Docker Machine is considered deprecated. You might be able to complete the course using it, but it the materials might
become incompatible with it in the future. No guide will be provided here, as it is not possible to keep it valid.

### Prepare the project

- Check out the [Scala Bootcamp](https://github.com/evolution-gaming/scala-bootcamp) project
- Navigate to `containers` folder
- Run integration tests (this project has only integration tests) from the command line using `sbt it:test`
- Open the project in IntelliJ IDEA

The tests will fail (for now), this is normal and expected.

### Troubleshooting

In case of issues:
- Read [Getting Started with Scala](https://docs.scala-lang.org/getting-started/index.html)
- Read [Discover IntelliJ IDEA for Scala](https://www.jetbrains.com/help/idea/discover-intellij-idea-for-scala.html)
- Ask in the Bootcamp chat

## Learning resources

All resources are listed in no particular order.

### Other

- [Docker playground](https://www.docker.com/play-with-docker)
- [Minikube tutorial](https://kubernetes.io/docs/tutorials/hello-minikube/)
- [Kubernetes courses](https://www.katacoda.com/courses/kubernetes) at Katacoda
- [Container courses](https://www.katacoda.com/courses/container-runtimes) at Katacoda

### Non-Scala

- [Learn Git Branching](https://learngitbranching.js.org/)

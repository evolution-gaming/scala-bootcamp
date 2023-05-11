# Akka Actors and Other Akka Features

## Actor Model
http://en.wikipedia.org/wiki/Actor_model

## Reference
https://doc.akka.io/docs/akka/current/index-classic.html
https://doc.akka.io/docs/akka/current/typed/index.html

## Technical white paper: Akka A to Z
https://info.lightbend.com/rs/558-NCX-702/images/COLL-white-paper-akka-A-to-Z.pdf
https://www.slideshare.net/Lightbend/akka-revealed-a-jvm-architects-journey-from-resilient-actors-to-scalable-clusters

## Best practices
https://github.com/alexandru/scala-best-practices/blob/master/sections/5-actors.md

## Safe Actors and Actor Effects in Evo
https://github.com/evolution-gaming/safe-akka
https://github.com/evolution-gaming/akka-effect

## Courses
https://courses.edx.org/courses/course-v1:EPFLx+scala-reactiveX+2T2019/course/
https://rockthejvm.com/

---

### Akka libraries and modules:

- Akka Actors
- Akka Typed
- Akka Cluster
- Akka Streams
- Akka HTTP
- Akka Persistence
- .. and more https://doc.akka.io/docs/akka/

---

### Akka Actors overview

#### Introduction to the actor model

The Actor formalism was first published in 1973.
Actor languages and communications patterns formulated in 1986.
First commercial use in Erlang (concurrency model based on Actors) for Ericsson telecommunications platform in 1995.
Scala standard library rolled out its actors implementation in 2006 (deprecated since 2.11).
Akka was created in 2009.

The main goal is to simplify multi-threading programming:
- effective resource utilization
- avoid blocking, deadlocks, synchronization of shared state

Key Concepts:
- Actors
- Messages
- Message passing

Principles:
- Encapsulation
- Asynchrony
- Location transparency
- Failure handling

#### Actors in Akka

- [AkkaHelloWorld.scala](AkkaHelloWorld.scala)
- [ActorContext101.scala](ActorContext101.scala)
- [StatePatterns.scala](StatePatterns.scala)
- [CommunicationPatterns.scala](CommunicationPatterns.scala)
- [LongInitializationPattern.scala](LongInitializationPattern.scala)
- [LifecycleAndSupervision.scala](LifecycleAndSupervision.scala)
- [AkkaDispatchers.scala](AkkaDispatchers.scala)
- [InteractingWithFutures.scala](InteractingWithFutures.scala)

- [EventStream.scala](EventStream.scala)
- [Routing.scala](Routing.scala)
- [BankAccountExample.scala](BankAccountExample.scala)

- [Exercise1.scala](Exercise1.scala)
- [Exercise2.scala](Exercise2.scala)
- [Exercise3.scala](Exercise3.scala)

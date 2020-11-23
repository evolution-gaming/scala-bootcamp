# Akka Actors fundamentals

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

### Plan:
- ActorModel (akka hello world, actor trait, receive function)
- ActorContext (behavior switch and creating actors)
- MessageProcessing (state encapsulation, actor mailbox)
- Lifecycle and Supervisors (actors hierarchy, error handling)
- Dispatchers and LongInitializationPattern

### For self-learning
- Broadcasting
- EventStream
- Routing
- TestKit (inside homework)

---

### Actors overview

The Actor formalism was first published in 1973.
Actor languages and communications patterns formulated in 1986.
First commercial use in Erlang (concurrency model based on Actors) for Ericsson telecommunications platform in 1995.
Scala standard library rolled out its actors implementation in 2006 (deprecated since 2.11).
Akka was created in 2009.

The main goal is to simplify multi-threading programming:
- effective resource utilization
- avoid blocking, dead locks, synchronization of shared state

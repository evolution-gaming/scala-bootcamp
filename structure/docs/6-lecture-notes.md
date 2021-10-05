# Scala application structure

    0. Introduction
    1. SBT project
    2. Package by type/layer
    3. Package by feature/component

- Format:
    - Live coding session
    - Q&A

- https://github.com/evolution-gaming/scala-bootcamp/structure

## 1. SBT project

- `$ sbt`
- sbt files
    - [project](../project)
    - [build.sbt](../build.sbt)
- root files
    - `readme.md`, `.conf`, `.gitignore`
- code
    - [src/main/scala](../src/main/scala)
    - [src/test/scala](../src/test/scala)
    - [target](../target)
- modules
    - [app](../app/src/main/scala)
    - [domain](../domain)

## 2. Package by type/layer

1. By layer:
    - application
    - model
    - persistence
    - api
    - ui
    - utils/config

2. By type:
    - controllers
    - models
    - repositories
    - utils
    - exceptions
    - commands
    - actors
    - activities

### Tasks:

1. Dummy project with stubs and dependencies
2. Try to understand what service is doing
    - was it fast and straight forward?
    - inexpressive - top-level structure does not give you idea what app is about
3. Introduce some changes
    - add casino status 
      - forgot about ScheduledUpdates
    - update find user by id
      - how can we be sure we found all places?
      - need to check all packages
      - actually always see redundant code (other features in packages)
      - inexpressive - hard to identify dependencies
      - https://phauer.com/2020/package-by-feature/#package-by-layer
      - cyclic dependencies
4. Delete casino management (extracted to separate service)
   - some parts in wrong abstraction `UserRepository`
   - not all parts deleted `Utils.log`
   - was it fast and straight forward?
   - code to abstraction
     - dependency magnet, code gravity
     - update of abstraction affects unnecessary code `AbstractRepository`
     - wrong abstractions
     - merge conflicts
5. Fat/monolith structure
   - fat compilation (no parallelization)
   - packages are tightly coupled, can't "ship" in isolation (monolith)
6. Pros
    - Easy to start with
    - MVP - minimal viable project

### Can we do better?

## 3. Package by feature

### Tasks:

1. Try to identify features
2. Extract features to packages
3. Decouple services/controllers/repositories
   - read only dependencies
   - pass interface as dependency
   - interface segregation principle
4. Minimise common code (`Config`, `AbstractRepository`)
5. Initialize feature classes in module class
6. Try tasks from package by feature section
7. Pros
    - Fixes package by layer Cons
8. Cons
    - Feature identification
    - To which feature code belongs (`AssignmentController`)
9. Sources
    - https://phauer.com/2020/package-by-feature/#package-by-feature
    - http://www.javapractices.com/topic/TopicAction.do?Id=205
    - https://en.wikipedia.org/wiki/Loose_coupling
    - https://proandroiddev.com/package-by-type-by-layer-by-feature-vs-package-by-layered-feature-e59921a4dffa

### Package by component

1. Extension of package by feature
2. Feature becomes a component - contain other features inside
3. Candidate for separate deployable/microservice/repository

### Package features by modules (sbt projects)

1. Extract features to sbt modules
2. Cycle dependencies
3. `AssignmentController`

### Package by screens (views)

1. Applicable for apps with UI only

## 4. Other

- These are not only about package structure but rather about architecture:
    - Domain driven design
    - Hexagonal architecture (ports & adapters)
    - Clean/Onion architecture

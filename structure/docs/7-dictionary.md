# Dictionary

## Domain model

Domain model is an object model of the domain.

Domain model classes represents your domain/problem knowledge:
- real world objects (User, Game etc.)
- persistence entities (same User, Game etc.) if db is in place
- rules, roles, data types, dictionaries - as help of describing domain
- in scala often those will be a `case class`, `case object`, `val`

Domain model incorporates both behavior and data.
- you may have behaviour/logic methods inside classes:
  ```scala 
    case class User(id: UserId, position: Position, active: Boolean) {
      def changePosition(to: Position): User = copy(position = to)
      def delete: User = copy(active = false)
    } 
  ```

Sources:
  - https://martinfowler.com/eaaCatalog/domainModel.html
  - https://en.wikipedia.org/wiki/Domain_model

## Dependency injection (DI)

Dependency injection is providing an object with its dependencies (other objects) instead of having it to construct them itself.
It's a very useful technique for testing, since it allows dependencies to be mocked or stubbed out.

```scala
class UserMessageSender(
  userRepository: UserRepository, // injected dependency
  sender: MessageSender           // injected dependency
)
```

Sources:
- https://www.jamesshore.com/v2/blog/2006/dependency-injection-demystified
- https://en.wikipedia.org/wiki/Dependency_injection
- https://martinfowler.com/articles/injection.html `DI, Inversion of Conctorl (IoC)`

## Other

- https://martinfowler.com/articles/dipInTheWild.html - Dependency inversion principle
- https://martinfowler.com/bliki/DomainDrivenDesign.html - Domain Drive Design
- https://martinfowler.com/tags/application%20architecture.html - Application architectures

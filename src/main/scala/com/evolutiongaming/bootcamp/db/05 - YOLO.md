Start Scala REPL

# Setup, init of REPL:

```scala
import java.time.{LocalDate, Year}
import java.util.UUID
import doobie._
import doobie.implicits._
import doobie.util.ExecutionContexts
import doobie.implicits.javatime._
import cats._
import cats.data._
import cats.effect._
import cats.implicits._
import com.evolutiongaming.bootcamp.db.DbConfig._
import com.evolutiongaming.bootcamp.db.DbCommon._
import com.evolutiongaming.bootcamp.db.Author
import com.evolutiongaming.bootcamp.db.Book

// We need a ContextShift[IO] before we can construct a Transactor[IO]. The passed ExecutionContext
// is where nonblocking operations will be executed. For testing here we're using a synchronous EC.
implicit val cs = IO.contextShift(ExecutionContexts.synchronous)

implicit val uuidMeta: Meta[UUID] = Meta[String].timap(UUID.fromString)(_.toString)
implicit val yearMeta: Meta[Year] = Meta[Int].timap(Year.of)(_.getValue)

// A transactor that gets connections from java.sql.DriverManager and executes blocking operations
// on an our synchronous EC. See the chapter on connection handling for more info.
val xa = Transactor.fromDriverManager[IO](
  driver = dbDriverName,
  url = dbUrl,
  user = dbUser,
  pass = dbPwd,
  Blocker.liftExecutionContext(ExecutionContexts.synchronous) // just for testing
)

val y = xa.yolo
import y._

val setup = for {
  _ <- Fragment.const(createTableAuthorsSql).update.run.transact(xa)
  _ <- Fragment.const(createTableBooksSql).update.run.transact(xa)
  _ <- Fragment.const(populateDataSql).update.run.transact(xa)
} yield ()

setup.unsafeRunSync()
```

# Samples

## `.quick` usage instead of `.map(println)`

```scala
sql"select name from authors"
   .query[String]
   .nel
   .transact(xa)
   .map(println)
   .unsafeRunSync()
```

```scala
sql"select name from authors"
   .query[String]
   .quick
   .unsafeRunSync()
```

## Samples for `.stream` usage

```scala
sql"select name from authors"
   .query[String]
   .stream
   .take(1)
   .quick
   .unsafeRunSync()
```

```scala
val names = sql"select name from authors"
    .query[String]
    .stream
    .transact(xa)
names
    .take(5)
    .compile
    .toVector
    .unsafeRunSync()
    .foreach(println(_))
```

## using Shapeless

```scala
import shapeless.record.Record
type Rec = Record.`Symbol("id") -> String, Symbol("name") -> String, Symbol("birthday") -> java.util.Date`.T
sql"select id, name, birthday from authors"
    .query[Rec]
    .stream
    .take(1)
    .quick
    .unsafeRunSync()
```

## Sample for `Meta`
```scala
sql"select id, name, birthday from authors"
    .query[Author]
    .stream
    .take(1)
    .quick
    .unsafeRunSync()
```

```scala
final case class AuthorId(id: UUID)
final case class Author2(name: String, birthday: LocalDate)
sql"select id, name, birthday from authors"
    .query[(AuthorId, Author2)]
    .to[List]
    .map(_.toMap)
    .quick
    .unsafeRunSync()
```

## Sample for query validation using `.check`
```scala
sql"select id, name, birthday from authors"
    .query[Author]
    .check
    .unsafeRunSync()
```

## Sample for logging of queries
```scala
sql"select id, name, birthday from authors where id = $authorOdersky"
    .queryWithLogHandler[Author](LogHandler.jdkLogHandler)
    .nel
    .transact(xa)
    .map(println)
    .unsafeRunSync()
```


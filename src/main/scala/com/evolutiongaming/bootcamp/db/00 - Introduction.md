# Java Database Connectivity (JDBC)

Java Database Connectivity (JDBC) is the official Java API, which defines how a client may access DB.
This is generic API for all kinds of RDBMS.

JDBC provides generic way how to use `DriverManager` to deal in DB-agnostic way with statements and result 
sets.

JDBC provides a means for application to send SQL query to DB server, get back result and map the results
to known primitive data types.

# Lifespan of connection and its usage

When application wants to talk with DB, it asks connection pool for available connection and, in scope of 
connection, application prepares data, statements etc, opens and closes transactions, executes queries, 
gets result set, parses it and, when done, closes connection.

In case of some issues, JDBC throws one of `SQLException`s and it is up to application to deal with 
consequences.

For large and long-living applications, DB connections are maintained by special DB connection 
pools. It is done to speed up opening of connection and to automate correct resource handling.

More information:
* [official documentation, JDBC 4.2](https://docs.oracle.com/javase/8/docs/technotes/guides/jdbc/jdbc_42.html)
* [high level overview in Java](https://en.wikipedia.org/wiki/Java_Database_Connectivity)
* [Structured Query Language (SQL)](https://en.wikipedia.org/wiki/SQL)
* [SQL tutorial](https://www.w3schools.com/sql/)

# doobie

`doobie` is a functional library to make work with DB/SQL simpler, safer and effectful.

From the Book:
> `doobie` programs are values. You can compose small programs to build larger programs. 
> Once you have constructed a program you wish to run, you interpret it into an effectful 
> target monad of your choice (IO for example) and drop it into your main application wherever you like.

Basic building blocks:
* `sql"..."`, `fr0"..."` and `fr"..."` - complete query "fragment" with ability to interpolate variables, 
   similar to `s".."`
* `Get`, `Put`, `Meta`, `Read`, `Write` - implicit mappers between JDBC and Scala types
* `transactor` - SQL statement interpreter 

`doobie` programs usually are `ConnectionIO[A]` which can computed in context of `java.sql.Connection` and 
which returns value of type `A` as a result (or fails with an error).

Usually `doobie` programs are interpreted by `transactor`, which takes care of:
* providing `Connection`
* wrapping calls in SQL transactions (customizable)
* making rollbacks on errors
* cleanup, like closing of all opened resources after query is done
* returns "normal" effectful program, usually, some kind of `IO`

In nutshell, `transactor` provides a way how to transform `ConnectionIO[A]` to `IO[A]`.  

More information:
* [Very nice introduction by author](https://youtu.be/M5MF6M7FHPo)
* [Anatomy of a doobie query](https://camo.githubusercontent.com/48794728000ab130c9552ebd7f267767b56c3b20127b9b0f6f21801c7ad3da7c/68747470733a2f2f63646e2e7261776769742e636f6d2f74706f6c656361742f646f6f6269652d696e666f677261706869632f76302e362e302f646f6f6269652e737667)
* [Book of Doobie](https://tpolecat.github.io/doobie/docs/index.html)
* [doobie exercises](https://www.scala-exercises.org/doobie/)

# Slick

From official documentation:
> Slick (“Scala Language-Integrated Connection Kit”) is Lightbend’s Functional Relational Mapping (FRM) 
> library for Scala that makes it easy to work with relational databases.

If we will have some free time, we can talk about Slick.
It is `Future` base, not `IO` based, while Slick's `DBIO[A]` is very similar to `doobie`'s `ConnectionIO[A]`. 

More information:
* [Official site](https://scala-slick.org/docs/)
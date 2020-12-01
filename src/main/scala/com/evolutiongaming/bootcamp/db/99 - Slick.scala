package com.evolutiongaming.bootcamp.db
import com.typesafe.config.ConfigFactory

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import slick.basic.DatabasePublisher
import slick.jdbc.H2Profile.api._
import slick.lifted.{ProvenShape, ForeignKeyQuery}

// The main application
// mostly copied from:
// * https://github.com/slick/slick/blob/v3.3.3/samples/hello-slick/src/main/scala/HelloSlick.scala
// * https://github.com/slick/slick/blob/v3.3.3/samples/hello-slick/src/main/scala/Tables.scala
object HelloSlick extends App {
  val config = ConfigFactory.parseString(
    """
       |h2mem1 = {
       |  url = "jdbc:h2:mem:test1"
       |  driver = org.h2.Driver
       |  connectionPool = disabled
       |  keepAliveConnection = true
       |}
       |""".stripMargin,
  )

  val db = Database.forConfig("h2mem1", config)

  try {
    // The query interface for the Suppliers table
    val suppliers: TableQuery[Suppliers] = TableQuery[Suppliers]

    // the query interface for the Coffees table
    val coffees: TableQuery[Coffees] = TableQuery[Coffees]

    val setupAction: DBIO[Unit] = DBIO.seq(
      // Create the schema by combining the DDLs for the Suppliers and Coffees
      // tables using the query interfaces
      (suppliers.schema ++ coffees.schema).create,
      // Insert some suppliers
      suppliers += (101, "Acme, Inc.", "99 Market Street", "Groundsville", "CA", "95199"),
      suppliers += (49, "Superior Coffee", "1 Party Place", "Mendocino", "CA", "95460"),
      suppliers += (150, "The High Ground", "100 Coffee Lane", "Meadows", "CA", "93966"),
    )

    val setupFuture: Future[Unit] = db.run(setupAction)

    val f = setupFuture
      .flatMap { _ =>
        //#insertAction
        // Insert some coffees (using JDBC's batch insert feature)
        val insertAction: DBIO[Option[Int]] = coffees ++= Seq(
          ("Colombian", 101, 7.99, 0, 0),
          ("French_Roast", 49, 8.99, 0, 0),
          ("Espresso", 150, 9.99, 0, 0),
          ("Colombian_Decaf", 101, 8.99, 0, 0),
          ("French_Roast_Decaf", 49, 9.99, 0, 0),
        )

        val insertAndPrintAction: DBIO[Unit] = insertAction.map { coffeesInsertResult =>
          // Print the number of rows inserted
          coffeesInsertResult foreach { numRows =>
            println(s"Inserted $numRows rows into the Coffees table")
          }
        }
        //#insertAction

        val allSuppliersAction: DBIO[Seq[(Int, String, String, String, String, String)]] =
          suppliers.result

        val combinedAction: DBIO[Seq[(Int, String, String, String, String, String)]] =
          insertAndPrintAction andThen allSuppliersAction

        val combinedFuture: Future[Seq[(Int, String, String, String, String, String)]] =
          db.run(combinedAction)

        combinedFuture.map { allSuppliers =>
          allSuppliers.foreach(println)
        }

      }
      .flatMap { _ =>
        /* Streaming */

        val coffeeNamesAction: StreamingDBIO[Seq[String], String] =
          coffees.map(_.name).result

        val coffeeNamesPublisher: DatabasePublisher[String] =
          db.stream(coffeeNamesAction)

        coffeeNamesPublisher.foreach(println)

      }
      .flatMap { _ =>
        /* Filtering / Where */

        // Construct a query where the price of Coffees is > 9.0
        val filterQuery: Query[Coffees, (String, Int, Double, Int, Int), Seq] =
          coffees.filter(_.price > 9.0)

        // Print the SQL for the filter query
        println("Generated SQL for filter query:\n" + filterQuery.result.statements)

        // Execute the query and print the Seq of results
        db.run(filterQuery.result.map(println))

      }
      .flatMap { _ =>
        /* Update */

        // Construct an update query with the sales column being the one to update
        val updateQuery: Query[Rep[Int], Int, Seq] = coffees.map(_.sales)

        val updateAction: DBIO[Int] = updateQuery.update(1)

        // Print the SQL for the Coffees update query
        println("Generated SQL for Coffees update:\n" + updateQuery.updateStatement)

        // Perform the update
        db.run(updateAction.map { numUpdatedRows =>
          println(s"Updated $numUpdatedRows rows")
        })

      }
      .flatMap { _ =>
        /* Delete */

        // Construct a delete query that deletes coffees with a price less than 8.0
        val deleteQuery: Query[Coffees, (String, Int, Double, Int, Int), Seq] =
          coffees.filter(_.price < 8.0)

        val deleteAction = deleteQuery.delete

        // Print the SQL for the Coffees delete query
        println("Generated SQL for Coffees delete:\n" + deleteAction.statements)

        // Perform the delete
        db.run(deleteAction).map { numDeletedRows =>
          println(s"Deleted $numDeletedRows rows")
        }

      }
      .flatMap { _ =>
        /* Sorting / Order By */

        val sortByPriceQuery: Query[Coffees, (String, Int, Double, Int, Int), Seq] =
          coffees.sortBy(_.price)

        println(
          "Generated SQL for query sorted by price:\n" +
            sortByPriceQuery.result.statements,
        )

        // Execute the query
        db.run(sortByPriceQuery.result).map(println)

      }
      .flatMap { _ =>
        /* Query Composition */

        val composedQuery: Query[Rep[String], String, Seq] =
          coffees.sortBy(_.name).take(3).filter(_.price > 9.0).map(_.name)

        println(
          "Generated SQL for composed query:\n" +
            composedQuery.result.statements,
        )

        // Execute the composed query
        db.run(composedQuery.result).map(println)

      }
      .flatMap { _ =>
        /* Joins */

        // Join the tables using the relationship defined in the Coffees table
        val joinQuery: Query[(Rep[String], Rep[String]), (String, String), Seq] = for {
          c <- coffees if c.price > 9.0
          s <- c.supplier
        } yield (c.name, s.name)

        println("Generated SQL for the join query:\n" + joinQuery.result.statements)

        // Print the rows which contain the coffee name and the supplier name
        db.run(joinQuery.result).map(println)

      }
      .flatMap { _ =>
        /* Computed Values */

        //#maxPrice
        // Create a new scalar value that calculates the maximum price
        val maxPrice: Rep[Option[Double]] = coffees.map(_.price).max
        //#maxPrice

        println("Generated SQL for max price column:\n" + maxPrice.result.statements)

        // Execute the computed value query
        db.run(maxPrice.result).map(println)

      }
      .flatMap { _ =>
        //#plainSql
        /* Manual SQL / String Interpolation */

        // A value to insert into the statement
        val state = "CA"

        // Construct a SQL statement manually with an interpolated value
        val plainQuery = sql"select SUP_NAME from SUPPLIERS where STATE = $state".as[String]
        //#plainSql

        println("Generated SQL for plain query:\n" + plainQuery.statements)

        // Execute the query
        db.run(plainQuery).map(println)

      }
    Await.result(f, Duration.Inf)

  } finally db.close

  // A Suppliers table with 6 columns: id, name, street, city, state, zip
  class Suppliers(tag: Tag) extends Table[(Int, String, String, String, String, String)](tag, "SUPPLIERS") {

    // This is the primary key column:
    def id: Rep[Int] = column[Int]("SUP_ID", O.PrimaryKey)
    def name: Rep[String] = column[String]("SUP_NAME")
    def street: Rep[String] = column[String]("STREET")
    def city: Rep[String] = column[String]("CITY")
    def state: Rep[String] = column[String]("STATE")
    def zip: Rep[String] = column[String]("ZIP")

    // Every table needs a * projection with the same type as the table's type parameter
    def * : ProvenShape[(Int, String, String, String, String, String)] =
      (id, name, street, city, state, zip)
  }

  // A Coffees table with 5 columns: name, supplier id, price, sales, total
  class Coffees(tag: Tag) extends Table[(String, Int, Double, Int, Int)](tag, "COFFEES") {

    def name: Rep[String] = column[String]("COF_NAME", O.PrimaryKey)
    def supID: Rep[Int] = column[Int]("SUP_ID")
    def price: Rep[Double] = column[Double]("PRICE")
    def sales: Rep[Int] = column[Int]("SALES")
    def total: Rep[Int] = column[Int]("TOTAL")

    def * : ProvenShape[(String, Int, Double, Int, Int)] =
      (name, supID, price, sales, total)

    // A reified foreign key relation that can be navigated to create a join
    def supplier: ForeignKeyQuery[Suppliers, (Int, String, String, String, String, String)] =
      foreignKey("SUP_FK", supID, TableQuery[Suppliers])(_.id)
  }
}

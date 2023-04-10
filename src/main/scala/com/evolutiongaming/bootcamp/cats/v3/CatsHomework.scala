package com.evolutiongaming.bootcamp.cats.v3

import cats.data.{NonEmptyList, NonEmptySet, OptionT}
import cats.kernel.{Monoid, Semigroup}
import cats.syntax.all._
import cats.{ApplicativeError, Id, Monad}

import java.time.YearMonth
import java.util.UUID
import scala.util.control.NoStackTrace

object CatsHomework {
  object MonoidUsage {
    type Client = String
    type Kind   = String
    // Problem id will be a random UUID
    case class Problem(kind: Kind, client: Client, id: UUID)

    /** A report item, aggregated from a number of Problem instances.
      *
      * @param total         Total number of problems
      * @param lastProblemId Id of last problem. Problems will be aggregated recent-first
      * @param kinds         Problem kinds seen, as a set
      * @param totalPerKind  Breakdown of total number of problems by kind
      */
    case class ClientReport(total: Int, lastProblemId: UUID, kinds: NonEmptySet[Kind], totalPerKind: Map[Kind, Int])
    object ClientReport {
      // Task: Define a Semigroup for ClientReport
      // Hint: each field has a Semigroup, e.g. you can use Semigroup.last[UUID] for lastProblemId
      implicit def clientReportSemigroup: Semigroup[ClientReport] = ???
    }

    // Task: Write a function to aggregate problems into a ClientReport for each affected Client
    def aggregate(input: Iterable[Problem]): Map[Client, ClientReport] = ???
  }

  object PolyValidation {

    /** Payment card
      *
      * @param number         Card number, must be 16 characters, only digits
      * @param expirationDate Expiration date, not validated
      * @param name           Cardholder name, only upper/lower-case latin characters and spaces, not empty
      * @param securityCode   Security code, 3 digits
      */
    case class PaymentCard(number: String, expirationDate: YearMonth, name: String, securityCode: String)

    sealed abstract class ValidationError extends Exception with NoStackTrace
    case object InvalidNumber             extends ValidationError
    case object InvalidName               extends ValidationError
    case object InvalidSecurityCode       extends ValidationError

    // Task: Implement card validation that can be run in any ApplicativeError
    // This should be possible to run in both Validated and Either
    def validate[F[_]](number: String, expirationDate: YearMonth, name: String, securityCode: String)(implicit
      F: ApplicativeError[F, NonEmptyList[ValidationError]]
    ): F[PaymentCard] = ???
  }

  object MonadTransformers {
    // How about a small key-value database?
    // Keys and value will be strings
    type Key   = String
    type Value = String

    // The database, polymorphic in effect type
    trait KVDatabase[F[_]] {
      def write(key: Key, value: Value): F[Unit]

      def read(key: Key): F[Option[Value]]
    }
    // Simple implementation running in Id with data in a mutable field
    // Tests will also use a different implementation
    class InMemoryStubDb(private var data: Map[String, String] = Map.empty) extends KVDatabase[cats.Id] {
      override def write(key: Key, value: Value): Id[Unit] = data += key -> value

      override def read(key: Key): Id[Option[Value]] = data.get(key)
    }

    // Raw request to the database, can contain garbage data
    case class RawRequest(
      op: String,
      key: Option[String] = None,
      value: Option[String] = None,
      previousValue: Option[String] = None,
    )
    // Possible responses
    // Actual response type will be F[Option[Response]]
    sealed trait Response
    object Response {
      case class Found(value: Value) extends Response
      case object Ok                 extends Response
    }

    // Parsed request, that can be executed directly on database
    sealed trait Request
    object Request {
      // Write a value, always returns Some(Ok)
      case class Write(key: Key, value: Value)                             extends Request
      // Read a value, returns Some(Found(_)) if value is present, None otherwise
      case class Read(key: Key)                                            extends Request
      // Read value for key, compare with expected, set to newValue if they are equal.
      // Return Some(Ok) if write happened, None otherwise
      case class CompareAndSet(key: Key, expected: Value, newValue: Value) extends Request
      // Read value for key, treat it as another key, read and return value at that key
      // E.g. ReadUnref("a") from ["a" -> "b", "b" -> "c", "c" -> "not this"] should return Some(Found("c"))
      case class ReadUnref(key: Key)                                       extends Request
    }

    // Task: implement parsing RawRequest into Request.
    // Each value in raw may not be present, return None in that case
    // Hint: mapN will help here
    def parseRequest(raw: RawRequest): Option[Request] = {
      raw.op match {
        case "write"      => ???
        case "read"       => ???
        case "cas"        => ???
        case "read-unref" => ???
        case _            => ???
      }
    }

    // Task: implement executing parsed request on database
    // Hint for CAS: if clauses in for-comprehensions work for OptionT
    // It's ok for CAS to be non-atomic, tests won't check anything concurrent
    def executeRequest[F[_]: Monad](db: KVDatabase[F], request: Request): OptionT[F, Response] = ???

    // Task: wire parsing and execution together to get the final interface to database
    def runReq[F[_]: Monad](db: KVDatabase[F], raw: RawRequest): F[Option[Response]] = ???
  }
}

package com.evolutiongaming.bootcamp.error_handling

object SmartConstructors extends App {

  // Question: decypher keywords
  sealed abstract case class Port private (value: Int)

  object Port {
    def of(n: Int): Option[Port] =
      if (n > 0 && n <= Short.MaxValue * 2 + 1) Some(new Port(n) {}) else None
  }

  sealed abstract case class IP private (value: String)

  object IP {
    private val regex = "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)"

    def of(string: String): Option[IP] =
      if (string.matches(s"\\b$regex\\.$regex\\.$regex\\.$regex")) Some(new IP(string) {}) else None
  }

  sealed abstract case class InetAddress private (ip: IP, port: Port)

  object InetAddress {
    def of(ip: String, port: Int): Either[String, InetAddress] = for {
      ip   <- IP.of(ip).toRight("Invalid IP")
      port <- Port.of(port).toRight("Invalid port")
    } yield new InetAddress(ip, port) {}
  }

  // Exercise:
  // Using algebraic data types and smart constructors, make it impossible to
  // construct a `BankAccount` with an illegal (undefined) state in the
  // business domain. Note any limitations in your solution.
  sealed abstract case class BankAccount private (ownerId: OwnerId, balance: BigDecimal, accountType: AccountType)

  object BankAccount {

    def of(ownerId: String, balance: BigDecimal, accountType: String): Either[String, BankAccount] = for {
      ownerId     <- OwnerId.of(ownerId).toRight("Invalid OwnerId")
      accountType <- AccountType.of(accountType).toRight("Invalid account type")
      balance     <- accountType match {
        case AccountType.Debit => Either.cond(balance >= 0, balance, "Balance must be positive")
        case _                 => Right(balance)
      }
    } yield new BankAccount(ownerId, balance, accountType) {}
  }

  sealed abstract case class OwnerId private (value: String)

  object OwnerId {
    def of(string: String): Option[OwnerId] =
      if (string.matches("[0-9]{10}")) Some(new OwnerId(string) {}) else None
  }

  sealed trait AccountType

  object AccountType {
    case object Debit  extends AccountType
    case object Credit extends AccountType

    def of(string: String): Option[AccountType] = string match {
      case "debit"  => Some(Debit)
      case "credit" => Some(Credit)
      case _        => None
    }
  }
}

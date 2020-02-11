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
  final case class BankAccount(ownerId: String, balance: BigDecimal, accountType: String)
}

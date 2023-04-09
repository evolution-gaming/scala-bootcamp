package com.evolutiongaming.bootcamp.tf

import cats._
import cats.implicits._
import cats.data._
import cats.effect._
import cats.effect.std.{Console, Random}

object Practically extends IOApp {
  object Exercise1 {
    final case class User(id: Int, age: Int)

    trait FindUser[F[_]] {
      def apply(id: Int): F[User]
    }

    def findFakeUserLocal[F[_]: Applicative]: FindUser[F] =
      new FindUser[F] {
        def apply(id: Int): F[User] =
          Applicative[F].pure(User(id, (id * 7 * 5) % 97))
      }

    // assume precisely users with ids 1..100 exist in our imaginary database
    // users must have different ids
    def findAgeMatch[F[_]: Monad](
      findUser: FindUser[F]
    ): F[Option[(User, User)]] = ???

    // // for different users
    // def findAgeMatch(
    //   findUser: FindUser[IO],
    // ): IO[Option[(User, User)]] = {
    //   findUser(1)
    //   println("user found by id 1")
    // }

    def main: IO[Unit] = {
      val line = s"findBirthdayMatch: ${Exercise1.findAgeMatch(Exercise1.findFakeUserLocal[Id])}"
      Console[IO].println(line)
    }
  }

  object Exercise2 {
    // lecture notes: mention parametricity

    trait BasicRandom[F[_]] {
      def nextIntBounded(i: Int): F[Int]
    }

    object BasicRandom {
      def fromRandom[F[_]](random: Random[F]): BasicRandom[F] =
        new BasicRandom[F] {
          def nextIntBounded(i: Int): F[Int] = random.nextIntBounded(i)
        }
    }

    // def largestOfThree[F[_]: Applicative: BasicRandom]: F[Int] =
    def largestOfThree[F[_]: Applicative](implicit
      random: BasicRandom[F]
    ): F[Int] =
      (
        random.nextIntBounded(100),
        random.nextIntBounded(100),
        random.nextIntBounded(100),
      ).mapN { case (a, b, c) =>
        List(a, b, c).max
      }

    def main: IO[Unit] =
      for {
        random         <- Random.scalaUtilRandom[IO].map(BasicRandom.fromRandom)
        largestOfThree <- largestOfThree[IO](implicitly[Applicative[IO]], random)
        line2           = s"largestOfThree: ${largestOfThree}"
        _              <- Console[IO].println(line2)
      } yield ()
  }

  override def run(args: List[String]): IO[ExitCode] =
    // lecture note: mention hindsight
    for {
      _ <- Exercise1.main
      _ <- Exercise2.main
    } yield ExitCode.Success
}

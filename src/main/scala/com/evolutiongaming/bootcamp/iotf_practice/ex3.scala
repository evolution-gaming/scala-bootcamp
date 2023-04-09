package com.evolutiongaming.bootcamp.iotf_practice

import cats.effect.IO
import zio.{Task, ZIO}

import scala.concurrent.Future

object ex3 {
  case class User(id: Long, login: String)

  /*
   * Old service from the company-common library
   */
  trait UnsafeUserRepository {
    def findUser(id: Long): Future[Option[User]]
    def addUser(user: User): Future[Unit]
  }

  trait UserRepository[F[_]] {
    def findUser(id: Long): F[Option[User]]
    def addUser(user: User): F[Unit]
  }
  object UserRepository {
    /*
     * Task: implement so that the service can be used from both `cats.IO` and `zio.Task`
     */
    def make[F[_]](unsafe: UnsafeUserRepository): UserRepository[F] =
      new UserRepository[F] {
        def findUser(id: Long): F[Option[User]] = ???
        def addUser(user: User): F[Unit]        = ???
      }
  }

  // Hint:
  def futureToIO[A](f: => Future[A]): IO[A]    = IO.fromFuture(IO.delay(f))
  def futureToZIO[A](f: => Future[A]): Task[A] = ZIO.fromFuture(_ => f)

  /*
   * Bonus task: make it possible to create the service from `cats.Id` for testing purposes
   */
}

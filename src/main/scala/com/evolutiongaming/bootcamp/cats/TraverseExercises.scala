package com.evolutiongaming.bootcamp.cats

import cats.effect.IO
import scala.concurrent.Future

// cats-core import everything
import cats._
import cats.data._
import cats.implicits._

object TraverseExercises extends App {
  import scala.concurrent.ExecutionContext.Implicits._

  /*
      map      : F[A]       .map      (A => B)    = F[B]
      flatMap  : F[A]       .flatMap  (A => F[B]) = F[B]
      flatten  : F[F[A]]    .flatten              = F[A]

      sequence : List[F[A]] .sequence             = F[List[A]]
      traverse : List[A]    .traverse (A => F[B]) = F[List[B]]

      also check out:

      flatTraverse
      traverseFilter
   */

  Future(2).map(_ + 3) == Future(5)
  Future(2).flatMap(x => Future(x + 3)) == Future(5)
  Future(Future(2)).flatten == Future(2)

  val exception = new Exception

  List(Future(2), Future(3)).sequence == Future(List(2, 3))
  List(Future(2), Future.failed(exception)).sequence == Future.failed(exception)

  List(2, 3).traverse(k => Future(k + 1)) == Future(List(3, 4))


  // API
  type UserId
  case class User(friends: List[UserId])

  val ids: List[UserId] = ???

  val getUser: UserId => Option[User] = ???

  val getUserAsync: UserId => IO[Option[User]] = ???

  // Task
  val users: List[Option[User]] = ids.map(id => getUser(id))
  val users2: List[User] = ???

  val usersAsync: List[IO[Option[User]]] = ids.map(id => getUserAsync(id))
  val usersAsync2: IO[List[User]] = ???

  val usersFriendsAsync: List[IO[Option[List[UserId]]]] = ids.map(id => getUserAsync(id).map(_.map(_.friends)))
  val usersFriendsAsync2: IO[List[UserId]] = ???

  // advanced
  val usersFriendsFriendsAsync: List[IO[Option[List[IO[Option[User]]]]]] = ids.map(id => getUserAsync(id).map(_.map(_.friends.map(id => getUserAsync(id)))))
  val usersFriendsFriendsAsync2: IO[List[User]] = ???

}

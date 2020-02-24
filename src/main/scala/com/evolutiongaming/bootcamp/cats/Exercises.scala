package com.evolutiongaming.bootcamp.cats

import cats.effect.IO

object Exercises {

  // cats-core import evetything
  import cats._
  import cats.data._
  import cats.implicits._

  // API
  type UserId
  case class User(friends: List[UserId])

  val ids: List[UserId] = ???

  val getUser: UserId => Option[User] = ???

  val getUserAsync: UserId => IO[Option[User]] = ???


  /*
    essential
      map      : F[A].map(A => B) = F[B]
      flatMap  : F[A].flatMap(A => F[B]) = F[B]
      sequence : List[F[A]].sequence = F[List[A]]

    convenient
      flatten  : F[F[A]].flatten = F[A]
      traverse : List[A].traverse(A => F[B]) = F[List[B]]
   */

  // List[User]
  val users: List[Option[User]] = ids.map(id => getUser(id))

  // IO[List[User]]
  val usersAsync: List[IO[Option[User]]] = ids.map(id => getUserAsync(id))

  // IO[List[UserId]]
  val usersFriendsAsync: List[IO[Option[List[UserId]]]] = ids.map(id => getUserAsync(id).map(_.map(_.friends)))

  // IO[List[User]]
  val usersFriendsFriendsAsync: List[IO[Option[List[IO[Option[User]]]]]] = ids.map(id => getUserAsync(id).map(_.map(_.friends.map(id => getUserAsync(id)))))

  // IO[List[User]]
  val usersFriendsFriendsFriendsAsync: List[IO[Option[List[IO[Option[List[IO[Option[User]]]]]]]]] = ids.map(id => getUserAsync(id).map(_.map(_.friends.map(id => getUserAsync(id)).map(_.map(_.map(_.friends.map(id => getUserAsync(id))))))))

  // Nested

}

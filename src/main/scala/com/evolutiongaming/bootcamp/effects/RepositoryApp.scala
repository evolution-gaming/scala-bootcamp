package com.evolutiongaming.bootcamp.effects

import cats.effect.IO

import scala.concurrent.Future
import scala.jdk.CollectionConverters._

import cats.effect.unsafe.implicits.global
import scala.concurrent.ExecutionContext.Implicits.{global => ec}

object RepositoryApp extends App {
  case class User(id: String)

  // TODO: implement in memory dao
  // doesn't have to be thread safe
  class UserDao {
    def getAllUsers: List[User] = ???

    def addUser(user: User): Unit = ???
  }

  def program = {
    val users = new UserDao

    val u1 = users.getAllUsers
    println(u1)
    users.addUser(User("Vera"))
    users.addUser(User("Katya"))
    val u2 = users.getAllUsers
    println(u2)
  }

  program

  // run IO
  // program.unsafeRunSync()

  // wait for future
  Thread.sleep(500)
}

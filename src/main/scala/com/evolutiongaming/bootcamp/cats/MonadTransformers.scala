package com.evolutiongaming.bootcamp.cats

import cats.syntax.all._
import cats.instances.all._
import cats.effect.IO

object For {

  // API
  type Error
  type UserId
  type OrderId
  type Item

  def getFriends(userId: UserId): Either[Error, List[UserId]] = ???
  def getOrders(userId: UserId): Either[Error, List[OrderId]] = ???
  def getItems(orderId: OrderId): Either[Error, List[Item]] = ???


  // implement
  def friendsOrders(userId: UserId): Either[Error, List[Item]] = ???
}


object MonadTransformers {

  // API
  type Error
  type UserId
  type OrderId
  type Item

  def getFriends(userId: UserId): IO[Either[Error, List[UserId]]] = ???
  def getOrders(userId: UserId): IO[Either[Error, List[OrderId]]] = ???
  def getItems(orderId: OrderId): IO[Either[Error, List[Item]]] = ???


  // implement
  def friendsOrders(userId: UserId): IO[Either[Error, List[Item]]] = ???

}

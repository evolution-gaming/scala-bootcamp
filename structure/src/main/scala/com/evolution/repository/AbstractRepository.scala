package com.evolution.repository

class AbstractRepository {
  type Query[T] = Option[T]

  def read[T]: Query[T]     = None
  def add[T](v: T): Unit    = ()
  def delete[T](v: T): Unit = ()

  implicit class QueryOps[T](val query: Query[T]) {
    def update(v: T): Unit = ()
  }
}

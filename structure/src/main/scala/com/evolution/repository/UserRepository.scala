package com.evolution.repository

import com.evolution.domain._

class UserRepository extends AbstractRepository {
  def userById(id: UserId): Option[User] = None
  def userPermissions(id: UserId): List[Permission] = Nil
  def userGroups(id: UserId): List[Group] = Nil

  def addUser(value: User): Unit = ()
  def addPermission(value: Permission): Unit = ()
  def addGroup(value: Group): Unit = ()

  def casinoUsersCount(casinoId: CasinoId): Int = 0
}

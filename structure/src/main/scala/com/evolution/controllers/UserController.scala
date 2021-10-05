package com.evolution.controllers

import com.evolution.domain._
import com.evolution.dto._
import com.evolution.infrastructure.http.Controller
import com.evolution.services._

import java.util.UUID

class UserController(
    userService: UserService,
    permissionService: PermissionService,
    groupService: GroupService
) extends Controller {
  // get: api/user/$userId
  def userById(id: String): Option[UserDto] =
    userService.userById(UserId(id)).map(UserDto.from)

  // get: api/user/$userId/permissions
  def userPermissions(id: String): List[PermissionDto] = Nil

  // get: api/user/$userId/groups
  def userGroups(id: String): List[GroupDto] = Nil

  // post: api/user
  def addUser(user: UserDto): Unit =
    userService.addUser(
      User(
        UserId(UUID.randomUUID().toString),
        UserLogin(user.login),
        UserFirstName(user.name),
        UserLastName(user.lastName)
      )
    )

  // get: api/user/$userId/report/$reportId
  def report(userId: String, reportId: String): Option[_] = None

  // get: api/user/$userId/analysis
  def analysis(userId: String): Option[_] = None
}

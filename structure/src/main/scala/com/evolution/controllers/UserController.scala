package com.evolution.controllers

import com.evolution.domain._
import com.evolution.dto._
import com.evolution.infrastructure.http.Controller
import com.evolution.services._

import java.util.UUID

class UserController(
  userService: UserService
) extends Controller {
  // get: api/user/$userId
  def userById(id: String): Option[UserDto] =
    userService.userById(UserId(id)).map(UserDto.from)

  // get: api/user/$userId/permissions
  def userPermissions(id: String): List[PermissionDto] =
    userService.userById(UserId(id)).toList.flatMap { user =>
      userService.userPermissions(user).map(PermissionDto.from)
    }

  // get: api/user/$userId/groups
  def userGroups(id: String): List[GroupDto] =
    userService.userById(UserId(id)).toList.flatMap { user =>
      userService.userGroups(user).map(GroupDto.from)
    }

  // post: api/user
  def addUser(user: UserDto): Unit =
    userService.addUser(
      User(
        UserId(UUID.randomUUID().toString),
        UserLogin(user.login),
        UserFirstName(user.name),
        UserLastName(user.lastName),
      )
    )

  // get: api/user/$userId/report/$reportId
  def report(userId: String, reportId: String): Option[_] =
    userService.userById(UserId(userId)).flatMap { user =>
      userService.report(user, ReportId(reportId))
    }

  // get: api/user/$userId/analysis
  def analysis(userId: String): Option[_] =
    userService.userById(UserId(userId)).flatMap { user =>
      userService.analysis(user)
    }
}

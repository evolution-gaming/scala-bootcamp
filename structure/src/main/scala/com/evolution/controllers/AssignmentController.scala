package com.evolution.controllers

import com.evolution.domain.{GroupName, PermissionName}
import com.evolution.infrastructure.http.Controller
import com.evolution.infrastructure.utils.Utils
import com.evolution.services._

class AssignmentController(
  userService: UserService
) extends Controller {
  // post: api/group/$groupId/permissions
  def assignPermissions(groupId: String, permissionIds: List[String]): Unit =
    userService.assignPermissions(
      GroupName(Utils.extractId(groupId)),
      permissionIds.map(PermissionName),
    )

  // post: api/user/$userId/groups
  def assignGroups(userId: String, groupIds: List[String]): Unit = ()
}

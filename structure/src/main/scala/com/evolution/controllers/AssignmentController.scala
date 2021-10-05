package com.evolution.controllers

import com.evolution.domain.{Group, GroupName, Permission, PermissionName}
import com.evolution.dto.{GroupDto, PermissionDto}
import com.evolution.infrastructure.http.Controller
import com.evolution.infrastructure.utils.Utils
import com.evolution.services._

class AssignmentController(
    userService: UserService,
    permissionService: PermissionService,
    groupService: GroupService
) extends Controller {
  // post: api/group/$groupId/permissions
  def assignPermissions(groupId: String, permissionIds: List[String]): Unit =
    userService.assignPermissions(
      GroupName(Utils.extractId(groupId)),
      permissionIds.map(PermissionName)
    )

  // post: api/user/$userId/groups
  def assignGroups(userId: String, groupIds: List[String]): Unit = ()

  // post: api/group
  def addGroup(group: GroupDto): Unit =
    groupService.addGroup(Group(GroupName(group.name)))

  // post: api/permission
  def addPermission(perm: PermissionDto): Unit =
    permissionService.addPermission(Permission(PermissionName(perm.name)))
}

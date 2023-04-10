package com.evolution.controllers

import com.evolution.domain.{Permission, PermissionName}
import com.evolution.dto.PermissionDto
import com.evolution.infrastructure.http.Controller
import com.evolution.services._

class PermissionController(permissionService: PermissionService) extends Controller {
  // post: api/permission
  def addPermission(perm: PermissionDto): Unit =
    permissionService.addPermission(Permission(PermissionName(perm.name)))

  // delete: api/permission
  def deletePermission(perm: PermissionDto): Unit =
    permissionService.deletePermission(Permission(PermissionName(perm.name)))
}

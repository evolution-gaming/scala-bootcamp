package com.evolution.controllers

import com.evolution.domain.{Group, GroupName}
import com.evolution.dto.GroupDto
import com.evolution.infrastructure.http.Controller
import com.evolution.services._

class GroupController(groupService: GroupService) extends Controller {
  // post: api/group
  def addGroup(group: GroupDto): Unit =
    groupService.addGroup(Group(GroupName(group.name)))

  // delete: api/group
  def deleteGroup(group: GroupDto): Unit =
    groupService.deleteGroup(Group(GroupName(group.name)))
}

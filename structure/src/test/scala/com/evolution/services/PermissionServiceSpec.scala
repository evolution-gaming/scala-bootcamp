package com.evolution.services

import com.evolution.domain.{Permission, PermissionName}
import com.evolution.repository.UserRepository
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class PermissionServiceSpec extends AnyWordSpec with Matchers {
  private val repositoryStub = new UserRepository
  private val service        = new PermissionService(repositoryStub)
  "PermissionService" should {
    "add permission" in {
      service.addPermission(Permission(PermissionName("Test"))) shouldBe ()
    }
  }
}

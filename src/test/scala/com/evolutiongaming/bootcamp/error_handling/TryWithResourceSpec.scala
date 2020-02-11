package com.evolutiongaming.bootcamp.error_handling

import com.evolutiongaming.bootcamp.error_handling.TryWithResource.Resource
import org.scalatest.TryValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.util.control.NoStackTrace

final class TryWithResourceSpec extends AnyFlatSpec with Matchers with TryValues {

  "Resource" should "work on normal conditions" in {
    var closed   = false
    val resource = new Resource[String]("Hello, World!", _ => closed = true)
    resource.use(_.toUpperCase).success.value shouldBe "HELLO, WORLD!"
    closed shouldBe true
  }

  it should "handle errors on open" in {
    val resource = new Resource[String](throw MyException, _ => throw Unexpected)
    resource.use(identity).failure.exception shouldBe MyException
  }

  it should "handle errors on use" in {
    val resource = new Resource[String]("Hello, World!", _ => ())
    resource.use(_ => throw MyException).failure.exception shouldBe MyException
  }

  it should "handle errors on close" in {
    val resource = new Resource[String]("Hello, World!", _ => throw MyException)
    resource.use(identity).failure.exception shouldBe MyException
  }

  it should "close resource on use errors" in {
    var closed = false
    new Resource[String]("Hello, World!", _ => closed = true).use(identity)
    closed shouldBe true
  }

  it should "not catch fatal errors on open" in {
    intercept[MyFatal.type] {
      new Resource[String](throw MyFatal, _ => throw Unexpected).use(identity)
    }
  }

  it should "not catch fatal errors on use" in {
    intercept[MyFatal.type] {
      new Resource[String]("Hello, World!", _ => ()).use(_ => throw MyFatal)
    }
  }

  it should "not catch fatal errors on close" in {
    intercept[MyFatal.type] {
      new Resource[String]("Hello, World!", _ => throw MyFatal).use(identity)
    }
  }

  it should "close resource on fatal use errors" in {
    var closed = false
    intercept[MyFatal.type] {
      new Resource[String]("Hello, World!", _ => closed = true).use(_ => throw MyFatal)
    }
    closed shouldBe true
  }

  private object MyException extends Exception with NoStackTrace
  private object MyFatal     extends VirtualMachineError with NoStackTrace
  private object Unexpected  extends Exception with NoStackTrace
}

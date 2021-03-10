import org.mockito.{MockitoScalaSession, Strictness}
import org.mockito.ArgumentMatchersSugar._
import org.mockito.IdiomaticMockito._

// When extending org.mockito.scalatest.IdiomaticMockito you get strict mocks
// and all mockito-scala perks, i.e.
// class MyTest extends WordSpec with IdiomaticMockito

trait Foo {
  def foo: Int
  def bar(n: Int): Int
}

MockitoScalaSession(strictness = Strictness.StrictStubs).run {
  val m = mock[Foo]

  m.foo returns 42

  val foo = m.foo

  m.foo wasCalled once

  foo
}

MockitoScalaSession(strictness = Strictness.StrictStubs).run {
  val m = mock[Foo]

  m.bar(1) returns 42
  m.bar(2) returns 77

  val _ = m.bar(1)
  val bar = m.bar(2)

  m.bar(*) wasCalled atLeastOnce

  bar
}

MockitoScalaSession(strictness = Strictness.StrictStubs).run {
  val m = mock[Foo]

  m.bar(*) answers identity[Int] _

  val bar = m.bar(42)

  m.bar(*) wasCalled atLeastOnce

  bar
}

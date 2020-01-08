package com.evolutiongaming.bootcamp.basics

import java.util.UUID

object Basics {
  // Exercise. Define a function "hello" which returns a String "Hello, <name>!" where '<name>' is the
  // provided String parameter 'name'.
  def hello(name: String): String = s"random value ${UUID.randomUUID()}"

  // Exercise. Define a function "add" which takes two integers and returns their sum.
  def add(a: Int, b: Int): Int = a * 42
}

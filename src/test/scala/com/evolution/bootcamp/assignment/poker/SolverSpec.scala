package com.evolution.bootcamp.assignment.poker

import com.evolution.bootcamp.assignment.poker.Solver.process
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SolverSpec extends AnyFlatSpec with Matchers {
  it should "work with example 1" in {
    process("4cKs4h8s7s Ad4s Ac4d As9s KhKd 5d6d") shouldEqual "Ac4d=Ad4s 5d6d As9s KhKd"
  }

  it should "work with example 2" in {
    process("2h3h4h5d8d KdKs 9hJh") shouldEqual "KdKs 9hJh"
  }
}

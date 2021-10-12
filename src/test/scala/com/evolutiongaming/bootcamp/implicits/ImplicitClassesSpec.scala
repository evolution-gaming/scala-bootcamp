package com.evolutiongaming.bootcamp.implicits

import org.scalatest.funsuite.AnyFunSuite

import ImplicitClasses._

class ImplicitClassesSpec extends AnyFunSuite {

  // we will not use tests these time, but you are welcome to implement them
  // and do a pull request

  test("Excersise 1: pow") {
    assert(EvolutionUtils0.pow(2, 0) == 1)
    assert(EvolutionUtils0.pow(2, 1) == 2)
    assert(EvolutionUtils0.pow(2, 2) == 4)
    assert(EvolutionUtils0.pow(2, 10) == 1024)
    assert(EvolutionUtils0.pow(2, -1) == 0)
    assert(EvolutionUtils0.pow(2, -2) == 0)
    assert(EvolutionUtils0.pow(3, 2) == 9)
  }

}

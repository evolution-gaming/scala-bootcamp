package com.evolution.bootcamp.assignment.poker

import com.evolution.bootcamp.assignment.poker.Solver.process
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SolverSpec extends AnyFlatSpec with Matchers {
  it should "work with example 1" in {
    process("4cKs4h8s7s Ad4s Ac4d As9s KhKd 5d6d") shouldEqual "Ac4d=Ad4s 5d6d As9s KhKd"
  }

  it should "work with example 2" in {
    process("2h3h4h5d6d KdKs 9hJh") shouldEqual "KdKs 9hJh"
  }

  it should "Random Test 3" in {
    process("9d9h3sJs5s JcQd 4d5h 7hQh") shouldEqual "7hQh 4d5h JcQd"
  }

  it should "Random Test 4" in {
    process("4sTd5h6d7d 3cTc 3d7c 5d6h") shouldEqual "5d6h 3cTc=3d7c"
  }

  it should "test for three pairs" in {
    process("5s5dThTd7s 4c4d 6c9c Qd7h") shouldEqual "4c4d 6c9c Qd7h"
  }

  it should "test for full house" in {
    process("2h3h3d3c9s 2c2d Tc5h Jd6s") shouldEqual "Tc5h Jd6s 2c2d"
  }

  it should "test for four of a kind" in {
    process("6c4h4s6s8s 4c4d QdTh Qs7h") shouldEqual "QdTh=Qs7h 4c4d"
  }

  it should "test for straight flush" in {
    process("6c7c8c6s9h 4c5c 9d6h") shouldEqual "9d6h 4c5c"
  }

  it should "test for high card" in {
    process("ThKs2s7hAh 4cJc 3dQd") shouldEqual "4cJc 3dQd"
  }

  it should "test for straight with preceding A" in {
    process("8d4h3h5h5s Ac2c Td4d") shouldEqual "Td4d Ac2c"
  }

  it should "test for two pairs split pot" in {
    process("4d4h6hJs7d 2c2d 2h2s") shouldEqual "2c2d=2h2s"
  }

  it should "test for pair split pot" in {
    process("4d3h6hJs7d 2c2d 2h2s") shouldEqual "2c2d=2h2s"
  }

  it should "test for three different pairs where two are tied" in {
    process("4d3h6hJs7d 2c2d 2h2s 8c8d") shouldEqual "2c2d=2h2s 8c8d"
  }

  it should "same thing, just switched order" in {
    process("4d3h6hJs7d 8c8d 2h2s 2c2d") shouldEqual "2c2d=2h2s 8c8d"
  }

  it should "test for both pairs but high card wins" in {
    process("2d3h6hJs7d 2hKs 2cQs") shouldEqual "2cQs 2hKs"
  }

  it should "test for triple two pairs split pot" in {
    process("2sQsQhTh5h 2c3c 2h6d 2d4d") shouldEqual "2c3c=2d4d=2h6d"
  }

  it should "same thing, but third player has Ah and wins" in {
    process("2sQsQhTh5h 2c3c 2h6d 2dAh") shouldEqual "2c3c=2h6d 2dAh"
  }

  it should "4 high-cards split pot" in {
    process("4cTcJd9h8s 2c3c 2d3d 2h3h 2s3s") shouldEqual "2c3c=2d3d=2h3h=2s3s"
  }

  it should "same thing, but 5th player wins with A-high" in {
    process("4cTcJd9h8s 2c3c 2d3d 2h3h 2s3s As5d") shouldEqual "2c3c=2d3d=2h3h=2s3s As5d"
  }

  it should "three pairs split pot " in {
    process("2s5s6hJdKc 2c3c 2d3d 2h3h") shouldEqual "2c3c=2d3d=2h3h"
  }

  it should "same thing, but there is A-pair" in {
    process("2s5s6hJdKc 2c3c 2d3d 2h3h AsAh") shouldEqual "2c3c=2d3d=2h3h AsAh"
  }

  it should "3 full houses" in {
    process("4c4d4hQhJs 2c2d 3d3c 2c2s") shouldEqual "2c2d=2c2s 3d3c"
  }

  it should "4 flushes, 3 tied" in {
    process("9c3cQcKcAc 2cJc 4c5c 6c7c 8cTc") shouldEqual "4c5c 6c7c 8cTc 2cJc"
  }

  it should "3 flushes split pot" in {
    process("8cTcJcQcKc 2c3c 4c5c 6c7c") shouldEqual "2c3c=4c5c=6c7c"
  }

  it should "Random Test 5" in {
    process("Ad5dQh4hQc TcTd 9sKd 2cTh Ah6c 2dJh QsQd 7cAc 8d9h") shouldEqual "8d9h 2cTh 2dJh 9sKd TcTd Ah6c 7cAc QsQd"
  }

  it should "three pairs with same base, two tied, one wins by high-card" in {
    process("2s5s6hJdKc 2c3c 2d3d 2hQh") shouldEqual "2c3c=2d3d 2hQh"
  }

  it should "Random Test 6" in {
    process("4h6d7h8d9h Ac6s Ah3d TcJh Td7s") shouldEqual "Ah3d Ac6s Td7s TcJh"
  }

  it should "Random Test 7" in {
    process("4d5hAcKhQd 9dKc 5s8h 2d7d 9s3h 2h3s 4cTd 2cAs ThJd") shouldEqual "2d7d 9s3h 4cTd 5s8h 9dKc 2cAs 2h3s ThJd"
  }
}

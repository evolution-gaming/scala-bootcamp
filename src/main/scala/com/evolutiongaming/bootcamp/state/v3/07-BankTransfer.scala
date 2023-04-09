package com.evolutiongaming.bootcamp.state.v3

import cats.effect.kernel.Ref
import cats.effect.{IO, IOApp}
import io.github.timwspence.cats.stm.STM
import cats.syntax.all._
import scala.concurrent.duration._

/** Software Transactional Memory */

/** We now know how to modify a piece of state atomically.
  * But what if we want to modify multiple pieces of state atomically?
  */
object BankTransferDemo extends IOApp.Simple {
  // question: how to check if there were any retries?
  def withdraw(account: Ref[IO, Int], amount: Int): IO[Unit] =
    account.access.flatMap { case (prev, setter) =>
      if (prev < amount) IO.raiseError(new Exception("insufficient funds"))
      else
        setter(prev - amount).flatMap {
          case false => withdraw(account, amount)
          case true  => IO.unit
        }
    }

  def deposit(account: Ref[IO, Int], amount: Int) = account.update(_ + amount)

  // problem: we have two atomic operations but they're not atomic together
  // because of that state can be inconsistent - sum of money in both accounts should always be the same
  def transfer(a: Ref[IO, Int], b: Ref[IO, Int], amount: Int) =
    for {
      _ <- withdraw(a, amount)
      _ <- deposit(b, amount)
    } yield ()

  override def run: IO[Unit] =
    for {
      a    <- Ref.of[IO, Int](100)
      b    <- Ref.of[IO, Int](0)
      _    <- List.fill(100)(0).parTraverse(_ => transfer(a, b, 1))
      aRes <- a.get
      bRes <- b.get
      _    <- IO.println(s"a: $aRes, b: $bRes")
    } yield ()
}

/**  Software Transactional Memory (STM) is a modular composable concurrency data structure.
  *  It allows us to combine and compose a group of memory operations and perform all of them in one single atomic operation.
  *
  *  It supports ACI properties:
  *  Atomicity - all updates either run once or not at all
  *  Consistency - reads always get consistent view of the state, no partial updates
  *  Isolation - multiple transactional updates are isolated and don't interfere with each other
  */

/**  transfer consists of two atomic operations - withdraw and deposit, but transfer itself isn't atomic
  *  possible solutions:
  *  - put all accounts in a single Ref (causes contention)
  *  - use pessimistic locking (error prone, can lead to deadlocks)
  *  - use STM
  */
object STMDemo extends IOApp.Simple {
  def run(stm: STM[IO]): IO[Unit] = {
    import stm._

    def withdraw(account: TVar[Int], amount: Int): Txn[Unit] =
      for {
        balance <- account.get
        _       <-
          if (balance < amount) stm.raiseError(new Exception())
          else account.set(balance - amount)
      } yield ()

    def deposit(account: TVar[Int], amount: Int): Txn[Unit] = account.modify(_ + amount)

    def transfer(a: TVar[Int], b: TVar[Int], amount: Int): IO[Unit] = {
      commit {
        for {
          _ <- withdraw(a, amount)
          _ <- deposit(b, amount)
        } yield ()
      }
    }

    for {
      a       <- commit(TVar.of(1000))
      b       <- commit(TVar.of(1000))
      _       <- (
        List.fill(1000)(0).parTraverse(_ => transfer(a, b, 1).attempt),
        List.fill(1500)(0).parTraverse(_ => transfer(b, a, 1).attempt),
      ).parTupled
      aResult <- commit(a.get)
      bResult <- commit(b.get)
      _       <- IO.println(s"result a: $aResult, result b: $bResult")
    } yield ()
  }

  override def run: IO[Unit] = STM.runtime[IO].flatMap(run)

  // exercise: see how STM retry works by using stm.check in the withdraw method in place of stm.raiseError
  // implement account, deposit money to this account in fixed time interval e.g. add 10 every 3 seconds
  // try to withdraw amount that's not initially present in the account (but will be after a couple of deposits)
}

/**  TMVar represents container that can be either empty or full
  *  it's similar to Deferred from cats-effect but we can fill it and then make it empty again
  */
object TMVarDemo extends IOApp.Simple {
  def run(stm: STM[IO]): IO[Unit] = {
    import stm._

    for {
      tm      <- commit(TMVar.empty[String])
      _       <- (IO.sleep(3.seconds) *> commit(tm.put("full")) *> IO.sleep(
        3.seconds
      ) *> commit(tm.put("full again"))).start
      result1 <- IO.println("waiting") *> commit(tm.take)
      _       <- IO.println(s"result: $result1")
      result2 <- IO.println("waiting again") *> commit(tm.take)
      _       <- IO.println(s"result: $result2")
    } yield ()
  }

  override def run: IO[Unit] = STM.runtime[IO].flatMap(run)
}

/** Transactional data structures:
  * - TQueue
  * - TSemaphore
  *
  * another implementation: ZIO STM
  *
  * Benefits of STM:
  *
  * 1. Composable
  * 2. Declarative
  * 3. Optimistic Concurrency
  * 4. Lock-free
  * 5. Fine-grained locking
  *
  * Implications of STM:
  *
  * 1. Can't do effects inside STM
  * 2. Large allocations
  */

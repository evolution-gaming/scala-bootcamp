package com.evolutiongaming.bootcamp.async

import java.util.concurrent.atomic.AtomicInteger

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future, Promise}
import scala.util.{Failure, Success}

object Threads extends App {
  new Thread(() => {
    println("Doing very parallel things")
  }).start()

  class HelloThread(toWhom: String) extends Thread {
    override def run(): Unit = {
      println(s"$getName: Hello, $toWhom!")
    }
  }

  new HelloThread("world").start()
  new HelloThread("world").start()

  Thread.sleep(5000L) //pausing current thread for 5000ms = 5s
  println("That's all, folks!")
}

object BasicFutures extends App {
  import scala.concurrent.ExecutionContext.Implicits.global

  val completedFuture: Future[Int] = Future.successful(42) //doesn't schedule work
  completedFuture.foreach(println)

  val failedFuture: Future[Int] = Future.failed(new RuntimeException("oh my")) //doesn't schedule work
  failedFuture.failed.foreach(t => t.printStackTrace())


  val futureFromBlock: Future[String] = Future {
    //code block is immediately scheduled for execution on the implicit execution context
    //if you throw an exception inside the block, it is converted to a failed future case
    println("doing work!")
    "work done!"
  }
  futureFromBlock.onComplete {
    case Success(value) => println(value)
    case Failure(t)     => t.printStackTrace()
  }
}

object FutureFromPromise extends App {
  def asyncInc(future: Future[Int])(implicit ec: ExecutionContext): Future[Int] = {
    val promise = Promise[Int]()

    future.onComplete {
      case Success(value) =>
        promise.success(value + 1) //can be called only once
      case Failure(t)     =>
        promise.failure(t) //can be called only once
    } //can be replaced with promise.complete(result: Try[T])
    //promise can be completed only once!

    promise.future
  }

  {
    import scala.concurrent.ExecutionContext.Implicits.global

    val future = Future {
      1
    }

    asyncInc(future).foreach(println)
  }
}

/*
  Implement firstCompleted.

  Result future should be completed with a result or a failure of a first one to complete from the 2 provided.
  Use promises and promise completion methods which doesn't fail on multiple attempts (only the first one
  succeeds):
  - trySuccess
  - tryFailure
  - tryComplete

  Add implicit args to the function if needed!
   */
object Exercise1 extends App {
  def firstCompleted[T](f1: Future[T], f2: Future[T])(implicit ec: ExecutionContext): Future[T] = ???

  {
    import scala.concurrent.ExecutionContext.Implicits.global

    val future1 = Future {
      Thread.sleep(1000L) //normally you don't use thread sleep for async programming in Scala
      123
    }
    val future2 = Future {
      Thread.sleep(500L)
      321
    }

    println(Await.result(firstCompleted(future1, future2), 5.seconds))
  }
}

object TransformFutures extends App {
  def asyncInc(future: Future[Int])(implicit ec: ExecutionContext): Future[Int] =
    future.map(_ + 1)

  def asyncSum2(f1: Future[Int], f2: Future[Int])(implicit ec: ExecutionContext): Future[Int] =
    f1.flatMap { value1 =>
      //value => Future
      f2.map { value2 =>
        value1 + value2
      }
    } //completes with success when both futures succeed, fails when either of those fail

  //nicer for-comprehension syntax
  def asyncMultiply(f1: Future[Int], f2: Future[Int])(implicit ec: ExecutionContext): Future[Int] =
    for {
      value1 <- f1
      value2 <- f2
    } yield value1 * value2
}

/*
Implement sumAll using collection foldLeft and map + flatMap on Future's (or for comprehension).
If called on an empty collection, should return Future.successful(0).
 */
object Exercise2 extends App {
  def sumAll(futureValues: Seq[Future[Int]])(implicit ec: ExecutionContext): Future[Int] = ???

  {
    import scala.concurrent.ExecutionContext.Implicits.global

    val futures = Vector.fill(10)(1).map(Future.successful)
    println(Await.result(sumAll(futures), 5.seconds)) //should see 10
  }
}

object FutureShenanigans {
  import scala.concurrent.ExecutionContext.Implicits.global
  //parallel or in sequence?

  def example1(f1: Future[Int], f2: Future[Int]): Future[Int] = {
    for {
      result1 <- f1
      result2 <- f2
    } yield result1 + result2
  }

  def example2(f1: => Future[Int], f2: => Future[Int]): Future[Int] = {
    for {
      result1 <- f1
      result2 <- f2
    } yield result1 + result2
  }
}

object SharedStateProblems extends App {
  import scala.concurrent.ExecutionContext.Implicits.global

  var counter: Int = 0

  val thread1 = Future {
    (1 to 1000).foreach(_ => counter += 1)
  }
  val thread2 = Future {
    (1 to 1000).foreach(_ => counter += 1)
  }
  Await.ready(thread1, 5.seconds)
  Await.ready(thread2, 5.seconds)
  println(counter)
}

object SharedStateSynchronized extends App {
  import scala.concurrent.ExecutionContext.Implicits.global

  //all variables which are read and written by multiple threads should be declared as volatile
  @volatile
  var counter: Int = 0

  def threadSafeInc(): Unit = synchronized {
    counter += 1
  }

  val thread1 = Future {
    (1 to 1000).foreach(_ => threadSafeInc())
  }
  val thread2 = Future {
    (1 to 1000).foreach(_ => threadSafeInc())
  }
  Await.ready(thread1, 5.seconds)
  Await.ready(thread2, 5.seconds)
  println(counter)
}

object SynchronizedDeadlock extends App {
  import scala.concurrent.ExecutionContext.Implicits.global

  val resource1 = new Object()
  val resource2 = new Object()

  val future1: Future[Unit] = Future {
    resource1.synchronized {
      Thread.sleep(100L)
      resource2.synchronized {
        println("Future1!")
      }
    }
  }
  val future2 = Future {
    resource2.synchronized {
      Thread.sleep(100L)
      resource1.synchronized {
        println("Future2!")
      }
    }
  }
  val resultFuture = for {
    _ <- future1
    _ <- future2
  } yield ()

  Await.ready(resultFuture, Duration.Inf)
}

object SharedStateAtomic extends App {
  import scala.concurrent.ExecutionContext.Implicits.global

  val counter: AtomicInteger = new AtomicInteger(0)

  val thread1 = Future {
    (1 to 1000).foreach(_ => counter.incrementAndGet())
  }
  val thread2 = Future {
    (1 to 1000).foreach(_ => counter.incrementAndGet())
  }
  Await.ready(thread1, 5.seconds)
  Await.ready(thread2, 5.seconds)
  println(counter)
}

/*
Make this work correctly a) first with synchronized blocks, b) then with AtomicReference
 */
object Exercise3 extends App {
  import scala.concurrent.ExecutionContext.Implicits.global
  val tasksCount = 100
  val taskIterations = 1000
  val initialBalance = 10


  //PLACE TO FIX - START
  var balance1: Int = initialBalance
  var balance2: Int = initialBalance

  def doTaskIteration(): Unit = {
    val State(newBalance1, newBalance2) = transfer(State(balance1, balance2))
    balance1 = newBalance1
    balance2 = newBalance2
  }

  def printBalancesSum(): Unit = {
    println(balance1 + balance2)
  }
  //PLACE TO FIX - FINISH


  def transfer(state: State): State = {
    if (state.balance1 >= state.balance2) {
      State(state.balance1 - 1, state.balance2 + 1)
    } else {
      State(state.balance1 + 1, state.balance2 - 1)
    }
  }

  val tasks = (1 to tasksCount).toVector.map(_ => Future {
    (1 to taskIterations).foreach(_ => doTaskIteration())
  })
  val tasksResultFuture: Future[Vector[Unit]] = Future.sequence(tasks)
  Await.ready(tasksResultFuture, 5.seconds)

  printBalancesSum() //should print 20

  final case class State(balance1: Int, balance2: Int)
}

object Singletons extends App {
  /*
  Properly implementing lazy initialized singletons which correctly work in a multithreading environment
  is a challenge. Luckily Scala got it for you!
   */

  lazy val immaLazyVal: String = {
    println("Lazy Val!")
    "value"
  }

  object Holder {
    val valInsideObject: String = {
      println("Val inside Object!")
      "value"
    }
  }

  println("Start")
  immaLazyVal
  immaLazyVal
  Holder.valInsideObject
  Holder.valInsideObject
  println("End")
}

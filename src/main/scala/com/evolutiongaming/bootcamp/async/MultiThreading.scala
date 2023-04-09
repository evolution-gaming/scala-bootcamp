package com.evolutiongaming.bootcamp.async

//  20 QUESTIONS SHOULD YOU ASK AI CHATBOT ABOUT Java\Scala Concurrency
//


//format: off



















/**
 * Questions 1-6
 * > What does ... mean in computer science ?
 * ╔═══════════════╗  
 * ║ B i g         ║  
 * ║ w o r d s     ║  
 * ╠═══════════════╣  
 * ║ Concurrent    ║  
 * ╟───────────────╢  
 * ║ Non-blocking  ║  
 * ╟───────────────╢  
 * ║ Asynchronous  ║  
 * ╟───────────────╢  
 * ║ Parallel      ║  
 * ╟───────────────╢  
 * ║ Multithreaded ║  
 * ╟───────────────╢  
 * ║ Synchronized  ║  
 * ╚═══════════════╝   
 */






 //format: on

/** Questions 7-9.
  *
  * > What is an OS process ?
  *
  * In an operating system, a process is something that is currently under execution. So, an active program can be
  * called a process. For example, when you want to search something on the web, then you start a browser. The browser
  * is a process that is currently under execution
  *
  * > What is an OS thread and how does it differ from an OS process?
  *
  * A thread is a subprocess or an execution unit within a process. A process can contain a single thread to multiple
  * threads. When a process starts, the operating system assigns the memory and resources to it. Each thread within a
  * process shares the memory and resources of that process only
  *
  * > What is a java thread?
  *
  * Java provides a Thread class to achieve thread programming. The Thread class provides constructors and methods to
  * create and perform operations on a thread. The Thread class extends the Object class and implements the Runnable
  * interface
  */

import java.util.concurrent.atomic.AtomicLong
import scala.collection.mutable
import scala.util.Random
import java.util.concurrent.atomic.AtomicReference
import scala.concurrent.Promise
import java.util.concurrent.ConcurrentLinkedQueue
import akka.actor.Actor
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.LinkedBlockingQueue

 //format: off
/** > Draw me a text art comprising multicore computer, OS, processes, threads and dispatcher
* _____________
* /            /|  _______   _______   _______
* /____________/ | /       | /       | /       |
* |           | |/|  CPU1 |||  CPU2 |||  CPU3 ||
* |           | / |_______|||_______|||_______||
* |           |/_________________________________|
* |           ||                                 |
* |    OS     ||          Dispatcher             |
* |___________||_________________________________|
*            ||                                  |
*            ||      Process A      Process B    |
*            ||     [Memory A]     [Memory B]    |
*            ||        /\              /\        |
*            ||       /  \            /  \       |
*            ||Thread1  Thread2 Thread3 Thread4  |
*            ||_________________________________ |
*/

//format: on  

object MultiThreading1 extends App {

  val other = new Thread(() => {
    Thread.sleep(2000)
    println("Hello from other thread")
  })

  other.setDaemon(true)
  other.start()
  println("Hello from the main thread")
}

//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//

object MultiThreading2 extends App {

  var x     = 0
  val other = new Thread(() => {
    println(s"other thread x = $x")
    x = 50
    Thread.sleep(2000)
    println(s"other thread x = $x")

  })

  other.start()

  Thread.sleep(300)
  println(s"main thread x = $x")
  x = 100

}

//format: off
/**
  * 
  * > Generate an ascii table of different OS kernels and processor architectures-
  * 
  * +-----------------+-----------------+-----------------+
  * | OS Kernel       | Processor       | Architecture    |
  * +-----------------+-----------------+-----------------+
  * | Linux           | Intel x86       | 32-bit or 64-bit|
  * |                 | ARM             | 32-bit or 64-bit|
  * |                 | MIPS            | 32-bit or 64-bit|
  * |                 | PowerPC         | 32-bit or 64-bit|
  * |                 | RISC-V          | 32-bit or 64-bit|
  * +-----------------+-----------------+-----------------+
  * | Windows         | Intel x86       | 32-bit or 64-bit|
  * |                 | AMD x86         | 32-bit or 64-bit|
  * |                 | ARM             | 32-bit or 64-bit|
  * +-----------------+-----------------+-----------------+
  * | BSD             | (Intel x86)     |  (32/64 bit)    |
  * |(macOS, FreeBSD, | (ARM)           | (ARM-based)     |
  * |(NetBSD, OpenBSD)| (Apple Silicon) |                 |
  * +(----------------------------------+-----------------+
  * | HP-UX           | PA-RISC         | RISC-based      |
  * |                 | Itanium         | EPIC-based      |
  * +-----------------+-----------------+-----------------+
  * | OS/2            | (Intel x86)     | (Protected mode |
  * |(16/32 bit)      | (16/32 bit)     |               |
  * +(----------------------------------+--------------------+
  * 
  */











//format: on
// Question 10.

// > WHAT IS Java Memory Model?

/** The Java Memory Model (JMM) is a specification that describes how threads in the Java programming language interact
  * through memory. It defines the allowable behavior of multithreaded programs, and therefore describes when memory
  * reorderings and optimizations are possible1.
  *
  * The JMM also defines the structure and organization of different memory spaces within the Java Virtual Machine
  * (JVM). These include:
  *
  * Heap memory: This is where objects are allocated and stored. It is divided into two parts: young generation and old
  * generation. The young generation contains newly created objects that are likely to be garbage collected soon. The
  * old generation contains long-lived objects that survive many rounds of garbage collection23. Non-heap memory: This
  * includes permanent generation (or metaspace since Java 8), which stores class metadata, constants, static variables,
  * and compiled code23. Stack memory: This is where local variables and method parameters are stored for each thread.
  * Each thread has its own stack that grows and shrinks as methods are invoked and returned23. The JMM helps developers
  * understand how to write correct and efficient concurrent programs in Java by providing guarantees about visibility,
  * atomicity, ordering, and synchronization of memory operations1.
  */

/** https://docs.oracle.com/javase/specs/jls/se20/html/jls-17.html
  */

object Volatility1 extends App {
  class Var(var value: Int)
  val v = new Var(0)

  val done = new AtomicLong(20)

  for (_ <- 0 until 10)
    new Thread(() => {
      for (_ <- 0 until 1_000_000)
        v.value += 1
      println(s"${Thread.currentThread().getName()} done")
      done.decrementAndGet()
    }).start()

  for (_ <- 0 until 10)
    new Thread(() => {
      for (_ <- 0 until 1_000_000)
        v.value -= 1
      println(s"${Thread.currentThread().getName()} done")
      done.decrementAndGet()
    }).start()

  while (done.get() > 0) {
    Thread.sleep(100)
  }

  println(s"Done ${v.value}")
}

//format: off















//format: on

/** Question 11.
  *
  * > What is synchronized keyword in java ?
  *
  * The synchronized keyword in Java marks a block or method a critical section. A critical section is where one and
  * only one thread is executing at a time, and the thread holds the lock for the synchronized section. The synchronized
  * keyword helps in writing concurrent parts of the applications, to protect shared resources within this block
  */

object Volatility2 extends App {
  class Var(var value: Int)
  val v = new Var(0)

  val done = new AtomicLong(20)

  for (_ <- 0 until 10)
    new Thread(() => {
      for (_ <- 0 until 1_000_000)
        v.synchronized { v.value += 1 }
      println(s"${Thread.currentThread().getName()} done")
      done.decrementAndGet()
    }).start()

  for (_ <- 0 until 10)
    new Thread(() => {
      for (_ <- 0 until 1_000_000)
        v.synchronized { v.value -= 1 }
      println(s"${Thread.currentThread().getName()} done")
      done.decrementAndGet()
    }).start()

  while (done.get() > 0) {
    Thread.sleep(100)
  }

  println(s"Done ${v.value}")
}



//format: off















//format: on

/** Question 12.
  *
  * > What is Atomic in Java ?
  *
  * An atomic in Java is a toolkit of variable java.util.concurrent.atomic package classes, which assist in writing lock
  * and wait-free algorithms with the Java language. An algorithm requiring only partial threads for constant progress
  * is lock-free. In a wait-free algorithm, all threads make progress continuously, even in cases of thread failure or
  * delay
  *
  * In Java, the reading and writing of 32-bit or smaller quantities are guaranteed to be atomic. By atomic, we mean
  * each action takes place in one step and cannot be interrupted. Thus, when we have multithreaded applications, the
  * read and write operations are thread-safe and need not be made synchronized
  */

object Volatility3 extends App {
  class Var(val value: AtomicLong = new AtomicLong)
  val v = new Var()

  val done = new AtomicLong(20)

  for (_ <- 0 until 10) {
    new Thread(() => {
      for (_ <- 0 until 1_000_000)
        v.value.incrementAndGet()
      println(s"${Thread.currentThread().getName()} done")
      done.decrementAndGet()
    }).start()
  }

  for (_ <- 0 until 10) {
    new Thread(() => {
      for (_ <- 0 until 1_000_000)
        v.value.decrementAndGet()
      println(s"${Thread.currentThread().getName()} done")
      done.decrementAndGet()
    }).start()
  }

  while (done.get() > 0) {
    Thread.sleep(100)
  }

  println(s"Done ${v.value}")
}

//
//
//
//
//
//
//
//
//
//
//
//

object ComplexState extends App {
  class WordCounter {
    val wordMap    = mutable.Map.empty[String, Int]
    val wordBuffer = mutable.ArrayBuffer.empty[String]

    def addWord(word: String): Unit = synchronized {
      if (wordMap.contains(word)) {
        wordMap(word) += 1
      } else {
        wordMap(word) = 1
        wordBuffer += word
      }
    }
  }

  val counter = new WordCounter
  val done    = new AtomicLong(10)

  val words = Array(
    "Abolition",
    "Abolitionist",
    "Abominable",
    "Abomination",
    "Aborigine",
    "Abortion",
    "Abrasive",
    "Abroad",
    "Abscess",
    "Abscond",
    "Absence",
    "Absent",
    "Absentee",
    "Absenteeism",
    "Absinthe",
    "Absolute",
    "Absolution",
    "Absolutism",
    "Absolve",
    "Absorb",
  )

  for (_ <- 0 until 10) {
    new Thread(() => {
      try {
        for (_ <- 0 until 1_000_000)
          counter.addWord(words(Random.nextInt(20)))
      } finally {
        println(s"${Thread.currentThread().getName()} done")
        done.decrementAndGet()
      }
    }).start()
  }

  while (done.get() > 0) {
    Thread.sleep(100)
  }

  println(s"""
  total count of words: ${counter.wordMap.values.sum}
  wordBuffer is the same as wordMap.keys: ${counter.wordBuffer.sorted == counter.wordMap.keys.toSeq.sorted}
  """)
}

/** Question 13. > What is AtomicReference in Java and how is it related to cats-effect/zio Ref type ?
  *
  * An AtomicReference in Java is a class that provides operations on an underlying object reference that can be read
  * and written atomically, and also contains advanced atomic operations
  *
  * The java.util.concurrent.atomic.AtomicReference class is designed to update variables in a thread-safe way It is a
  * reference to an object that may be updated atomically
  *
  * Functional programming is a programming paradigm that emphasizes the use of functions5 In Java, functional
  * programming can be achieved using the Java 8 lambda expressions and functional interfaces5
  *
  * The AtomicReference class can be used in functional programming to provide thread-safe access to a shared object
  * reference53124
  *
  * I hope that helps. Let me know if you have any other questions. The cats-effect/zio Ref type is a concurrency
  * primitive that provides a way to safely share mutable state between concurrent computations
  *
  * The Ref type is similar to the AtomicReference class in Java, but it is designed to work with the IO monad in
  * cats-effect and the ZIO monad in ZIO
  *
  * The Ref type is thread-safe and provides a way to update a value atomically
  */
object ComplexState2 extends App {

  case class WordCounter(
    wordMap: Map[String, Int] = Map.empty[String, Int],
    wordBuffer: Vector[String] = Vector.empty,
  ) {
    def addWord(word: String): WordCounter =
      wordMap.get(word) match {
        case Some(count) => copy(wordMap = wordMap.updated(word, count + 1))
        case None        => copy(wordMap = wordMap.updated(word, 1), wordBuffer = wordBuffer :+ word)
      }

  }

  val counter = new AtomicReference(WordCounter())
  val done    = new AtomicLong(10)

  val words = Array(
    "Abolition",
    "Abolitionist",
    "Abominable",
    "Abomination",
    "Aborigine",
    "Abortion",
    "Abrasive",
    "Abroad",
    "Abscess",
    "Abscond",
    "Absence",
    "Absent",
    "Absentee",
    "Absenteeism",
    "Absinthe",
    "Absolute",
    "Absolution",
    "Absolutism",
    "Absolve",
    "Absorb",
  )

  for (_ <- 0 until 10) {
    new Thread(() => {
      try {
        for (_ <- 0 until 1_000_000) {
          val word = words(Random.nextInt(20))
          counter.getAndUpdate(_.addWord(word))
        }
      } finally {
        println(s"${Thread.currentThread().getName()} done")
        done.decrementAndGet()
      }
    }).start()
  }

  while (done.get() > 0) {
    Thread.sleep(100)
  }

  println(s"""
  total count of words: ${counter.get.wordMap.values.sum}
  wordBuffer is the same as wordMap.keys: ${counter.get.wordBuffer.sorted == counter.get.wordMap.keys.toSeq.sorted}
  """)
}

/** Question 14.
  *
  * > What is context switching in programming, should one avoid it?
  *
  * In computer programming, context switching refers to storing an active process in its current state so the system’s
  * CPU can shift its resources to other tasks. A user can then instruct the CPU at any time to resume the process from
  * the same point. Context switching is a technique or method used by the operating system to switch a process from one
  * state to another to execute its function using CPUs in the system. When switching perform in the system, it stores
  * the old running process’s status in the form of registers and assigns the CPU to a new process to execute its tasks.
  *
  * Context switching is not inherently bad, but it can be a performance issue if it happens too frequently. This is
  * because the process of switching between contexts takes time and resources, which can slow down the system. However,
  * context switching is necessary for multitasking and is a fundamental part of modern operating systems
  */

/** Question 15.
  *
  * > What is the cost of having too many active threads in a process?
  *
  * The cost of having too many active threads in a process can be high. When a process has too many threads, the
  * operating system has to spend more time switching between them, which can slow down the system. Additionally, each
  * thread requires its own stack space, which can consume a lot of memory. If there are too many threads, the system
  * may run out of memory, which can cause the program to crash. In general, it’s best to limit the number of threads in
  * a process to the number of available cores on the system.
  *
  * However, the optimal number of threads depends on the specific application and the hardware it’s running on. In some
  * cases, having more threads than cores can improve performance, while in other cases, it can hurt performance
  */

/** Question 16.
  *
  * > What is thread pool in java?
  *
  * A thread pool in Java is a group of worker threads that are waiting for the job and reused many times. In the case
  * of a thread pool, a group of fixed-size threads is created. A thread from the thread pool is pulled out and assigned
  * a job by the service provider. After completion of the job, the thread is contained in the thread pool again. This
  * offers a solution to the problem of thread cycle overhead and resource thrashing. Since the thread is already
  * existing when the request arrives, the delay introduced by thread creation is eliminated, making the application
  * more responsive
  */

/** Question 17.
  *
  * > What is ExecutionContext in scala?
  *
  * In Scala, the ExecutionContext trait is defined in the scala.concurrent package. It offers similar functionality to
  * that of Executor objects. Many Scala methods take ExecutionContext objects as implicit parameters. It implements the
  * abstract execute method, which exactly corresponds to the execute method on the Executor interface, and the
  * reportFailure method. A custom ExecutionContext may be appropriate to execute code which blocks on IO or performs
  * long-running computations. ExecutionContext.fromExecutorService and ExecutionContext.fromExecutor are good ways to
  * create a custom ExecutionContext. The intent of ExecutionContext is to lexically scope code execution. That is, each
  * method, class, file, package, or application determines how to run its own code. This avoids issues such as running
  * application callbacks on a thread pool belonging to a networking library
  */

object ComplexStateEC extends App {

  import scala.concurrent.ExecutionContext

  class WordCounter {
    val wordMap    = mutable.Map.empty[String, Int]
    val wordBuffer = mutable.ArrayBuffer.empty[String]

    def addWord(word: String): Unit = synchronized {
      if (wordMap.contains(word)) {
        wordMap(word) += 1
      } else {
        wordMap(word) = 1
        wordBuffer += word
      }
    }
  }
  val counter = new WordCounter
  val done    = new AtomicLong(10)

  val words = Array(
    "Abolition",
    "Abolitionist",
    "Abominable",
    "Abomination",
    "Aborigine",
    "Abortion",
    "Abrasive",
    "Abroad",
    "Abscess",
    "Abscond",
    "Absence",
    "Absent",
    "Absentee",
    "Absenteeism",
    "Absinthe",
    "Absolute",
    "Absolution",
    "Absolutism",
    "Absolve",
    "Absorb",
  )

  def addWordTask() = counter.addWord(words(Random.nextInt(20)))

  def runTasks(name: String, count: Long)(task: => Any)(implicit ec: ExecutionContext): Unit =
    if (count > 0) {
      ec.execute(() => {
        task
        runTasks(name, count - 1)(task)
      })
    } else {
      println(s"$name done")
      done.decrementAndGet()
    }

  import ExecutionContext.Implicits.global
  for (i <- 0 until 10)
    runTasks(s"task-$i", 1_000_000)(addWordTask())

  while (done.get() > 0) {
    Thread.sleep(100)
  }

  println(s"""
    total count of words: ${counter.wordMap.values.sum}
    wordBuffer is the same as wordMap.keys: ${counter.wordBuffer.sorted == counter.wordMap.keys.toSeq.sorted}
    """)
}

/** Question 18.
  *
  * > What is the Future type in scala ?
  *
  * In Scala, Future is a type that represents a result of an asynchronous computation that may or may not be available
  * yet. When a new Future is created, Scala spawns a new thread and executes its code. Once the execution is finished,
  * the result of the computation (value or exception) will be assigned to the Future12.
  *
  * Future is used to create a temporary pocket of concurrency that you use for one-shot needs. You typically use it
  * when you need to call an algorithm that runs an indeterminate amount of time — such as calling a web service or
  * executing a long-running algorithm — so you therefore want to run it off of the main thread2.
  *
  * Scala provides combinators such as flatMap, foreach, and filter used to compose futures in a non-blocking way3.
  */

object ComplexStateFuture extends App {

  import scala.concurrent.ExecutionContext
  import scala.concurrent.Future
  import scala.concurrent.duration._
  import scala.concurrent.Await

  case class WordCounter(
    wordMap: Map[String, Int] = Map.empty[String, Int],
    wordBuffer: Vector[String] = Vector.empty,
  ) {
    def addWord(word: String): WordCounter = {
      wordMap.get(word) match {
        case Some(count) => copy(wordMap = wordMap.updated(word, count + 1))
        case None        => copy(wordMap = wordMap.updated(word, 1), wordBuffer = wordBuffer :+ word)
      }
    }
  }

  object counter {
    val state                                                              = new AtomicReference(WordCounter())
    def addWord(word: String)(implicit ec: ExecutionContext): Future[Unit] =
      Future(state.updateAndGet(_.addWord(word)))
  }

  val done = new AtomicLong(10)

  val words = Array(
    "Abolition",
    "Abolitionist",
    "Abominable",
    "Abomination",
    "Aborigine",
    "Abortion",
    "Abrasive",
    "Abroad",
    "Abscess",
    "Abscond",
    "Absence",
    "Absent",
    "Absentee",
    "Absenteeism",
    "Absinthe",
    "Absolute",
    "Absolution",
    "Absolutism",
    "Absolve",
    "Absorb",
  )

  def task()(implicit ec: ExecutionContext): Future[Unit] =
    Future(counter.addWord(words(Random.nextInt(20))))

  def runTasks(name: String, count: Long)(task: => Future[Unit])(implicit ec: ExecutionContext): Future[Unit] =
    if (count > 0)
      task.flatMap(_ => runTasks(name, count - 1)(task))
    else
      Future {
        println(s"$name done")
        done.decrementAndGet()
      }

  import ExecutionContext.Implicits.global

  val result = Future.traverse(0 until 10 to Vector)(i => runTasks(s"task-$i", 1_000_000)(task()))

  Await.result(result, Duration.Inf)

  val wordCounter = counter.state.get()

  println(s"""
      total count of words: ${wordCounter.wordMap.values.sum}
      wordBuffer is the same as wordMap.keys: ${wordCounter.wordBuffer.sorted == wordCounter.wordMap.keys.toSeq.sorted}
      """)
}

/** Question 19.
  *
  * > What is the Promise in scala?
  *
  * In Scala, Promise is a class that provides a way to complete a Future manually. A Promise is a writable, single
  * assignment container which sets the value of the future.
  *
  * A Future is a read-only placeholder view of a variable. By default, futures and promises are non-blocking, making
  * use of callbacks instead of typical blocking operations. To simplify the use of callbacks both syntactically and
  * conceptually, Scala provides combinators such as flatMap, foreach, and filter used to compose futures in a
  * non-blocking way2.
  *
  * Is there anything else I can help you with?
  */

object ComplexStateActor extends App {
  import scala.concurrent.ExecutionContext
  import scala.concurrent.Future
  import scala.concurrent.duration._
  import scala.concurrent.Await
  import java.util.concurrent.ConcurrentLinkedQueue

  val words = Array(
    "Abolition",
    "Abolitionist",
    "Abominable",
    "Abomination",
    "Aborigine",
    "Abortion",
    "Abrasive",
    "Abroad",
    "Abscess",
    "Abscond",
    "Absence",
    "Absent",
    "Absentee",
    "Absenteeism",
    "Absinthe",
    "Absolute",
    "Absolution",
    "Absolutism",
    "Absolve",
    "Absorb",
  )

  sealed trait CounterMessage
  case class AddWord(word: String)                    extends CounterMessage
  case class GetWordCount(result: Promise[Long])      extends CounterMessage
  case class AreWordInOrder(result: Promise[Boolean]) extends CounterMessage

  type ActorRef[A] = BlockingQueue[A]

  def ask[Mes, Res](ref: ActorRef[Mes], result: Promise[Res] => Mes): Future[Res] = {
    val promise = Promise[Res]()
    ref.offer(result(promise))
    promise.future
  }

  def counter(ref: ActorRef[CounterMessage]) = new Thread(() => {
    var wordMap: Map[String, Int]  = Map.empty[String, Int]
    var wordBuffer: Vector[String] = Vector.empty

    def addWord(word: String): Unit = {
      wordMap.get(word) match {
        case Some(count) =>
          wordMap = wordMap.updated(word, count + 1)
        case None        =>
          wordMap = wordMap.updated(word, 1)
          wordBuffer = wordBuffer :+ word
      }
    }

    while (true) {
      ref.take() match {
        case AddWord(word)          => addWord(word)
        case GetWordCount(result)   => result.success(wordMap.values.sum)
        case AreWordInOrder(result) => result.success(wordBuffer.sorted == wordMap.keys.toSeq.sorted)
      }
    }
  })

  def task(ref: ActorRef[CounterMessage])(implicit ec: ExecutionContext): Future[Unit] =
    Future(ref.offer(AddWord(words(Random.nextInt(20)))))

  def runTasks(name: String, count: Long)(task: => Future[Unit])(implicit ec: ExecutionContext): Future[Unit] =
    if (count > 0)
      task.flatMap(_ => runTasks(name, count - 1)(task))
    else
      Future {
        println(s"$name done")
      }

  def job(implicit ec: ExecutionContext): Future[String] = {
    val ref          = new LinkedBlockingQueue[CounterMessage]()
    counter(ref).start()
    def runFutures() = Future.sequence(
      for (i <- 0 until 10)
        yield runTasks(s"task-$i", 1_000_000)(task(ref))
    )

    for {
      _       <- runFutures()
      count   <- ask(ref, GetWordCount)
      inOrder <- ask(ref, AreWordInOrder)
    } yield s"""|total count of words: $count
                |wordBuffer is the same as wordMap.keys: $inOrder
                | """.stripMargin
  }

  {
    import ExecutionContext.Implicits.global
    println(Await.result(job, Duration.Inf))
  }

}

/** Question 20.
  *
  * > What are the popular approaches to concurrency in scala?
  *
  * 1. Actors
  *
  * 2. Futures
  *
  * 3. Reactive and other concurrent Streams
  *
  * 4. IO-based
  */

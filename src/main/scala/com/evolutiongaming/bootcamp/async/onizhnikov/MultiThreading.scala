package com.evolutiongaming.bootcamp.async.onizhnikov

import java.util.concurrent.atomic.AtomicLong

//  20 QUESTIONs SHOULD YOU ASK AI Chatbot about Java Concurrency?
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
 * ║ Asyncronous   ║  
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

//   other.setDaemon(true)
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

  var x = 0
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

  for (_ <- 0 until 10) {
    new Thread(() => {
      for (_ <- 0 until 1_000_000)
        v.value += 1
      println(s"${Thread.currentThread().getName()} done")
      done.decrementAndGet()
    }).start()
  }

  for (_ <- 0 until 10) {
    new Thread(() => {
      for (_ <- 0 until 1_000_000)
        v.value -= 1
      println(s"${Thread.currentThread().getName()} done")
      done.decrementAndGet()
    }).start()
  }

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
  * keyword helps in writing concurrent parts of the applications, to protect shared resources within this block123
  */

object Volatility2 extends App {
  class Var(var value: Int)
  val v = new Var(0)

  val done = new AtomicLong(20)

  for (_ <- 0 until 10) {
    new Thread(() => {
      for (_ <- 0 until 1_000_000)
        v.synchronized { v.value += 1 }
      println(s"${Thread.currentThread().getName()} done")
      done.decrementAndGet()
    }).start()
  }

  for (_ <- 0 until 10) {
    new Thread(() => {
      for (_ <- 0 until 1_000_000)
        v.synchronized { v.value -= 1 }
      println(s"${Thread.currentThread().getName()} done")
      done.decrementAndGet()
    }).start()
  }

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
  * delay1
  *
  * In Java, the reading and writing of 32-bit or smaller quantities are guaranteed to be atomic. By atomic, we mean
  * each action takes place in one step and cannot be interrupted. Thus, when we have multithreaded applications, the
  * read and write operations are thread-safe and need not be made synchronized234
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

package com.evolutiongaming.bootcamp.async.onizhnikov

//  WHAT QUESTION SHOULD YOU ASK AI Chatbot about Java Concurrency?
//


//format: off
/**
 * > What is the ... ?
 * > What is the difference between ... and ... ?
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

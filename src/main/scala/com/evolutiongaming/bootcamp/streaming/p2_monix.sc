import cats.effect.IO
import monix.eval.Task
import monix.execution.{Cancelable, Scheduler}
import monix.reactive.{Consumer, Observable, OverflowStrategy}

import scala.collection.immutable.ArraySeq
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scala.util.chaining.scalaUtilChainingOps

// Special scheduler to run streams in worksheets
// Usually it comes from TaskApp, or Scheduler.Implicits.global
implicit val scheduler: Scheduler = Scheduler.apply(ExecutionContext.parasitic)

val inputStr =
  """foo bar baz
    |qux bar baz
    |foO Foo fOO """.stripMargin

val lines: Observable[String] = Observable.fromIterator(Task(inputStr.linesIterator))

// Monix doesn't have a separate type for intermediate stages
// Replacement: write a function, use .pipe from scala.util.chaining._, 2.13+
def splitWords(in: Observable[String]): Observable[String] = {
  in.flatMapIterable(line => ArraySeq.unsafeWrapArray(line.split("\\s+")))
}

val wordCount: Task[Map[String, Int]] = lines
  .dump("lines")
  .pipe(splitWords)
  .dump("words")
  .map(_.toLowerCase)
  .scanMap(word => Map(word -> 1))
  .dump("counts")
  .lastL

wordCount.runSyncUnsafe()

// Task[T] can be converted to other CE-compatible F[_]s, e.g. IO
val wordCountIO: IO[Map[String, Int]] = wordCount.to[IO]

// For reuse in following samples
val words = lines.pipe(splitWords)

// Inputs
Observable(1, 2, 3)
Observable.fromIterable(Seq(1, 2, 3))

// Task => single-element Observable
Observable
  .fromTask(Task {
    println("Side-effect!")
    42
  })
  .toListL
  .runSyncUnsafe()

// Also works for IO
Observable
  .fromTaskLike(IO {
    println("Also side-effect!")
    43
  })
  .toListL
  .runSyncUnsafe()

// Construct from single-element effect types
Observable.from(Task(42))
Observable.from(IO(42))
Observable.from(Future.successful(42))
Observable.from(Try(42))
Observable.from(io.circe.parser.parse("[]")) // From Either[Throwable, T]
Observable.from(Seq(1, 2, 3))

// Ticks
val ticksSample = Observable
  .intervalWithFixedDelay(100.millis, 400.millis)
  .dump("ticks")
  .takeByTimespan(1.second)
  .completedL
//ticksSample.runSyncUnsafe()

// Observable.create is useful for wrapping callback-style APIs
trait AsyncCallbackyApi[T] {
  // Will call passed receiveElement on each value
  // () => Unit is cancellation token
  def subscribe(receiveElement: T => Unit): () => Unit
}
def callbackyToObservable[T](api: AsyncCallbackyApi[T]): Observable[T] = {
  Observable.create(OverflowStrategy.Fail(1024)) { subscriber =>
    val cancelToken = api.subscribe(element => subscriber.onNext(element))
    Cancelable(cancelToken)
  }
}

// Processing
// Async/Effects

// A => Task[B]
words.mapEval(word => Task(word.toLowerCase))
// For generic F[_], e.g. IO
words.mapEvalF(word =>
  IO {
    println(s"Processing $word")
    word.toLowerCase
  }
)

// "generic F[_]" includes Either
Observable("{}", "[1, 2, 3]")
  .mapEvalF(io.circe.parser.parse)
  .toListL
  .runSyncUnsafe()

// Parallel versions
words.mapParallelOrdered(4)(word => Task(println(word)))
words.mapParallelUnorderedF(4)(word => IO(println(word)))

// FSMs

// scan doesn't emit seed element at start
words
  .scan("!") { case (acc, word) => acc + word.take(1) }
  .dump("scan")
  .completedL
  .runSyncUnsafe()
// scan0 does. Same is true for other scan variations
words
  .scan0("!") { case (acc, word) => acc + word.take(1) }
  .dump("scan0")
  .completedL
  .runSyncUnsafe()

// Async version
words.scanEval(Task("!")) { case (acc, word) => Task(acc + word.take(1)) }
words.scanEvalF(IO("!")) { case (acc, word) => IO(acc + word.take(1)) }

// mapAccumulate, takes (State, T) => (State, Output), returns Observable[Output]
def zipWithPrevious[T](in: Observable[T]): Observable[(T, T)] = {
  in.mapAccumulate(Option.empty[T]) {
    case (None, value)       => Some(value) -> Option.empty[(T, T)]
    case (Some(prev), value) => Some(value) -> Option(prev -> value)
  }.collect { case Some(value) => value }
}

words
  .pipe(zipWithPrevious)
  .dump("words-prev")
  .completedL
  .runSyncUnsafe()

// doOn* for callbacks on various stream stages
lines
  .doOnSubscribe(Task(println("OnSubscribe")))
  .doOnStart(first => Task(println(s"Start: $first"))) // on first emitted element
  .doOnNext(word => Task(println(s"OnNext: $word")))
  .doOnCompleteF(IO(println("OnComplete")))
  .guaranteeCase(exitCase => Task(println(s"guarantee: $exitCase"))) // cats.effect.ExitCase
  .completedL
  .runSyncUnsafe()

// Sinks
// Basic sinks (returning Task[_]) are available directly on Observable
words.lastL
words.lastOptionL
words.firstL
words.toListL
words.completedL

// There is also consumer api
words.consumeWith(Consumer.foreachTask(word => Task(println(word))))
words.consumeWith(Consumer.foreachEval(word => IO(println(word))))

// Version returning F[_]
words.consumeWithF[IO, String](Consumer.head)
// Or you can call .to[F] on Task
words.headL.to[IO]

// foldLeft is also available, along with async foldLeftEval
words.consumeWith {
  Consumer.foldLeft("!") { case (acc, word) => acc + word.take(1) }
}

// Consumer.loadBalance runs several consumers in parallel, each element is consumed only once
words
  .consumeWith {
    Consumer.loadBalance(3, Consumer.toList[String])
  }
  .runSyncUnsafe()

// Shorthand for load-balanced foreach
Consumer.foreachParallelTask[String](4)(word => Task(println(word)))

// Splits, merges, non-linear pipelines
val duellingInts = Observable(
  Observable(1, 2, 3).delayOnNext(10.millis),
  Observable(4, 5, 6).delayOnNext(10.millis),
)
duellingInts.merge.toListL.runSyncUnsafe()
duellingInts.concat.toListL.runSyncUnsafe()
duellingInts.delayOnNext(25.millis).switch.toListL.runSyncUnsafe()

// There are versions of those combined with map()
// Those take A => Observable[B] argument and combine results in corresponding way
words.mergeMap(_ => Observable.empty)
words.concatMap(_ => Observable.empty)
words.switchMap(_ => Observable.empty)
// flatMap is concatMap
words.flatMap(_ => Observable.empty)

// groupBy returns stream of (stream per group)
words
  .groupBy(_.take(1))
  .mergeMap(group => group.last.map(group.key -> _))
  .toListL
  .runSyncUnsafe()

// combineLatest emits a tuple when either of arguments emits an item
Observable
  .combineLatest2(
    Observable(1, 2, 3).delayOnNext(20.millis),
    Observable(4, 5, 6).delayOnNext(30.millis),
  )
  .toListL
  .runSyncUnsafe()
// There is combineLatestMap* taking a function to map tuple to something else

// withLatestFrom is similar to combineLatest, but emits only on items from left side
words
  .delayOnNext(5.millis)
  .withLatestFrom(Observable.intervalWithFixedDelay(1.milli))(_ -> _)
  .toListL
  .runSyncUnsafe()

// Multiple consumers
val wordsLogSubscribe = words.doOnStart(_ => Task(println("Starting")))

// Subscribe to an observable twice - collect elements, and count them
def countAndCollect[T](in: Observable[T]): Task[(Long, List[T])] = {
  Task.parZip2(in.countL, in.toListL)
}

// Observables are split into "cold" and "hot" ones
// Cold Observables start entire stream from scratch on each subscribe
// All examples before were on cold observables

// Hot observables share the same running stream

// This will subscribe to cold observable twice,
// effectively running previous stream twice
countAndCollect(wordsLogSubscribe)
  .runSyncUnsafe()

// Can be avoided with publishSelector
wordsLogSubscribe
  .publishSelector { hot: Observable[String] => // can be subscribed multiple times
    val results = countAndCollect(hot)
    Observable.from(results) // Return single-element Observable with results
  }
  .lastL
  .runSyncUnsafe()

// Split processing example
Observable
  .range(1, 10)
  .publishSelector { hot =>
    Observable(
      hot.filter(_ % 2 != 0).map(odd => s"Odd: $odd"),
      hot.filter(_ % 2 == 0).map(even => s"Even: $even"),
    ).merge
  }

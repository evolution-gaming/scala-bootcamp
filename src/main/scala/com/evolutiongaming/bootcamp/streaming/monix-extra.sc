import cats.effect.concurrent.MVar
import monix.eval.Task
import monix.execution.Scheduler
import monix.reactive.{Observable, Pipe}
import monix.reactive.observables.ConnectableObservable

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.util.chaining.scalaUtilChainingOps


implicit val scheduler: Scheduler = Scheduler.apply(ExecutionContext.parasitic)

// Example: Emit one element per interval, drop the rest
def throttleSample[T](interval: FiniteDuration)(in: Observable[T]): Observable[T] = {
  Observable.from(MVar.empty[Task, Unit]).flatMap { loopBack =>
    val resets = Observable.repeatEvalF(loopBack.take).delayOnNext(interval)

    in.publishSelector { hot =>
      Observable(
        hot.map(Right(_)),
        resets.map(Left(_))
          .takeUntil(hot.completed) // ensure end stream completes when input completes
      ).merge
    }.mapAccumulate(true) {
      case (true, Right(element)) => false -> Some(element)
      case (false, Right(_)) => false -> None
      case (_, Left(_)) => true -> None
    }.collect { case Some(value) => value }
      .doOnNext(_ => loopBack.tryPut(()).void)
  }
}

val ints = Observable.from(1 to 10)

ints.delayOnNext(100.millis).pipe(throttleSample(250.millis))
  .dump("throttle-manual").completedL.runSyncUnsafe(10.seconds)

// Also available in monix
ints.delayOnNext(100.millis).throttleFirst(250.millis)
  .dump("throttle-monix").completedL.runSyncUnsafe()



// Example: buffer which collects items and emits once per interval
def bufferTimed[T](interval: FiniteDuration)(in: Observable[T]): Observable[Seq[T]] = {
  in.publishSelector { hot =>
    val ticks = Observable.intervalWithFixedDelay(interval).takeUntil(hot.completed)
    hot.bufferWithSelector(ticks)
  }
}

val ints = Observable.fromIterable(1 to 10)
val intsLogSubscribe = ints.doOnSubscribe(Task(println("Starting")))

def countAndCollect[T](in: Observable[T]): Task[(Long, List[T])] = {
  Task.parZip2(in.countL, in.toListL)
}



// Hot Observables
// CAUTION: impure API

// This is ConnectableObservable
// Doesn't run when subscribed, only starts running when .connect() is called
// Subscribes upstream only once (on connect()), can send elements to multiple downstream consumers
// .publish just broadcasts elements, there are others
val publishedInts: ConnectableObservable[Int] = intsLogSubscribe.publish
publishedInts.headL
//  .timeout(1.second).attempt.runSyncUnsafe() // will fail

// Example: call connect() directly
{
  for {
    published <- Task(intsLogSubscribe.publish)
    resultFiber <- countAndCollect(published).start
    cancel <- Task(published.connect())
    result <- resultFiber.join
    _ <- Task(cancel.cancel())
  } yield result
}
  .runSyncUnsafe()

// There's also .refCount
// Will call connect() on first subscription, and cancel() once all subscriptions are gone
val rc = intsLogSubscribe
  .delayExecution(10.millis)
  .publish.refCount

countAndCollect(rc).runSyncUnsafe()

// Be careful, refCount will NOT subscribe again after cancelling
countAndCollect(rc).runSyncUnsafe()

// Other types of hot observables have different behavior on subscription
// They will always emit items what arrive after subscribing, it's just initial behavior that's different
Task {
  // Emits nothing
  ints.publish
  // Emits most recent item from upstream, or supplied initial value if empty
  ints.behavior("")
  // Emits last n items from upstream
  ints.replay(3)
}


// Still, hot observables are impure api, and can be tricky to work with
// General advice is to stick to publishSelector
// For behavior/replay/others, there's more general pipeThroughSelector

// pipeThroughSelector:
//   transforms observable into hot one using specified pipe
//   applies function to hot observable
//   returns pure observable with result
intsLogSubscribe
  .pipeThroughSelector(
    Pipe.behavior[Int](-1),
    (hot: Observable[Int]) => Observable.from(countAndCollect(hot)))
  .lastL.runSyncUnsafe()
